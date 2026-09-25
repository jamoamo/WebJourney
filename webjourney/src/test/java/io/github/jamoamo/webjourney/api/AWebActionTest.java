/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.ActionResult;
import io.github.jamoamo.webjourney.BaseJourneyActionException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class AWebActionTest
{
	private static final String MDC_KEY = "WebJourney.Action";

	/**
	 * An action whose behaviour on each attempt (numbered from 1) is supplied by the test.
	 */
	private static final class TestAction extends AWebAction
	{
		private final IntFunction<ActionResult> behaviour;
		private final AtomicInteger attempts = new AtomicInteger();

		TestAction(IntFunction<ActionResult> behaviour)
		{
			this.behaviour = behaviour;
		}

		@Override
		protected ActionResult executeActionImpl(IJourneyContext context) throws BaseJourneyActionException
		{
			return this.behaviour.apply(this.attempts.incrementAndGet());
		}

		@Override
		protected String getActionName()
		{
			return "Test";
		}

		int getAttempts()
		{
			return this.attempts.get();
		}
	}

	private IJourneyContext newContext(IJourneyObserver... observers)
	{
		ITravelOptions options = Mockito.mock(ITravelOptions.class);
		Mockito.when(options.getRetryPolicy())
			.thenReturn(RetryPolicyBuilder.builder().maxRetries(3).delay(Duration.ofMillis(1)).build());
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getOptions()).thenReturn(options);
		Mockito.when(context.getJourneyObservers()).thenReturn(List.of(observers));
		return context;
	}

	@Test
	public void testExecuteAction_failsThenSucceeds_observerIsToldOfEachRetry()
	{
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		RuntimeException first = new IllegalStateException("first");
		RuntimeException second = new IllegalStateException("second");
		TestAction action = new TestAction(attempt ->
		{
			if(attempt == 1)
			{
				throw first;
			}
			if(attempt == 2)
			{
				throw second;
			}
			return ActionResult.SUCCESS;
		});

		ActionResult result = action.executeAction(newContext(observer));

		assertEquals(ActionResult.SUCCESS, result);
		Mockito.verify(observer).actionRetried(action, 1, first);
		Mockito.verify(observer).actionRetried(action, 2, second);
		Mockito.verify(observer, Mockito.never()).actionRetryAborted(Mockito.any(), Mockito.anyInt(), Mockito.any());
	}

	@Test
	public void testExecuteAction_retriesExhausted_observerIsToldOfEachRetryAndThenOfTheAbort()
	{
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		IllegalStateException failure = new IllegalStateException("always broken");
		TestAction action = new TestAction(attempt ->
		{
			throw failure;
		});

		assertThrows(BaseJourneyActionException.class, () -> action.executeAction(newContext(observer)));

		assertEquals(4, action.getAttempts());
		Mockito.verify(observer).actionRetried(action, 1, failure);
		Mockito.verify(observer).actionRetried(action, 2, failure);
		Mockito.verify(observer).actionRetried(action, 3, failure);
		Mockito.verify(observer).actionRetryAborted(action, 4, failure);
	}

	@Test
	public void testExecuteAction_refusal_observerIsToldOfTheAbortAndNotOfAnyRetry()
	{
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		ConnectException refusal = new ConnectException("Connection refused");
		TestAction action = new TestAction(attempt ->
		{
			throw new RuntimeException("navigation failed", refusal);
		});

		assertThrows(BaseJourneyActionException.class, () -> action.executeAction(newContext(observer)));

		assertEquals(1, action.getAttempts());
		Mockito.verify(observer, Mockito.never()).actionRetried(Mockito.any(), Mockito.anyInt(), Mockito.any());
		Mockito.verify(observer).actionRetryAborted(Mockito.eq(action), Mockito.eq(1), Mockito.any(RuntimeException.class));
	}

	@Test
	public void testExecuteAction_succeedsFirstTime_observerIsToldNothingAboutRetries()
	{
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		TestAction action = new TestAction(attempt -> ActionResult.SUCCESS);

		action.executeAction(newContext(observer));

		Mockito.verify(observer, Mockito.never()).actionRetried(Mockito.any(), Mockito.anyInt(), Mockito.any());
		Mockito.verify(observer, Mockito.never()).actionRetryAborted(Mockito.any(), Mockito.anyInt(), Mockito.any());
	}

	@Test
	public void testExecuteAction_observerThatIgnoresTheRetryHooks_stillWorks()
	{
		IJourneyObserver observer = new IJourneyObserver()
		{
			@Override
			public void actionStarted(AWebAction action)
			{
			}

			@Override
			public void actionEnded(AWebAction action)
			{
			}

			@Override
			public void entityCreated(Object object)
			{
			}

			@Override
			public void entityCreationStarted(Class<?> entityClass)
			{
			}
		};
		TestAction action = new TestAction(attempt ->
		{
			if(attempt == 1)
			{
				throw new IllegalStateException("flaky");
			}
			return ActionResult.SUCCESS;
		});

		assertEquals(ActionResult.SUCCESS, action.executeAction(newContext(observer)));
	}

	@Test
	public void testExecuteAction_nonRetryableFailure_isRethrownOnceWithoutRetryOrRetryHooks()
	{
		IJourneyObserver observer = Mockito.mock(IJourneyObserver.class);
		IllegalStateException failure = new IllegalStateException("stop here");
		TestAction action = new TestAction(attempt ->
		{
			throw new NonRetryableActionException(failure);
		});

		BaseJourneyActionException thrown = assertThrows(BaseJourneyActionException.class,
			() -> action.executeAction(newContext(observer)));

		assertSame(failure, thrown.getCause());
		assertEquals(1, action.getAttempts());
		Mockito.verify(observer, Mockito.never()).actionRetried(Mockito.any(), Mockito.anyInt(), Mockito.any());
		Mockito.verify(observer, Mockito.never()).actionRetryAborted(Mockito.any(), Mockito.anyInt(), Mockito.any());
	}

	@Test
	public void testNonRetryableActionException_nullFailure_isRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> new NonRetryableActionException(null));
	}

	@Test
	public void testExecuteAction_enclosingActionMdcLabelIsRestoredAfterwards()
	{
		MDC.put(MDC_KEY, "Outer");
		try
		{
			TestAction action = new TestAction(attempt -> ActionResult.SUCCESS);

			action.executeAction(newContext());

			assertEquals("Outer", MDC.get(MDC_KEY));
		}
		finally
		{
			MDC.remove(MDC_KEY);
		}
	}

	@Test
	public void testExecuteAction_enclosingActionMdcLabelIsRestoredAfterAFailureToo()
	{
		MDC.put(MDC_KEY, "Outer");
		try
		{
			TestAction action = new TestAction(attempt ->
			{
				throw new NonRetryableActionException(new IllegalStateException("stop"));
			});

			assertThrows(BaseJourneyActionException.class, () -> action.executeAction(newContext()));

			assertEquals("Outer", MDC.get(MDC_KEY));
		}
		finally
		{
			MDC.remove(MDC_KEY);
		}
	}

	@Test
	public void testExecuteAction_noEnclosingAction_labelIsRemovedAfterwards()
	{
		MDC.remove(MDC_KEY);
		TestAction action = new TestAction(attempt -> ActionResult.SUCCESS);

		action.executeAction(newContext());

		assertNull(MDC.get(MDC_KEY));
	}
}

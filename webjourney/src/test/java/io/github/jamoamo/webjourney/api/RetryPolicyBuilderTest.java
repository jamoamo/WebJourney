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

import io.github.jamoamo.webjourney.BaseJourneyActionException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class RetryPolicyBuilderTest
{
	private IRetryPolicy policy()
	{
		return RetryPolicyBuilder.builder().maxRetries(3).delay(Duration.ofMillis(1)).build();
	}

	@Test
	public void testExecute_transientFailure_isRetriedUntilItSucceeds() throws Exception
	{
		AtomicInteger calls = new AtomicInteger();

		String result = policy().execute(() ->
		{
			if(calls.incrementAndGet() < 3)
			{
				throw new IllegalStateException("flaky");
			}
			return "ok";
		});

		assertEquals("ok", result);
		assertEquals(3, calls.get());
	}

	@Test
	public void testExecute_persistentFailure_isRetriedUpToTheLimit()
	{
		AtomicInteger calls = new AtomicInteger();

		assertThrows(IllegalStateException.class, () -> policy().execute(() ->
		{
			calls.incrementAndGet();
			throw new IllegalStateException("always broken");
		}));

		assertEquals(4, calls.get());
	}

	@Test
	public void testExecute_chromeConnectionRefused_isNotRetried()
	{
		AtomicInteger calls = new AtomicInteger();

		WebDriverException refusal = new WebDriverException("unknown error: net::ERR_CONNECTION_REFUSED");

		WebDriverException thrown = assertThrows(WebDriverException.class, () -> policy().execute(() ->
		{
			calls.incrementAndGet();
			throw refusal;
		}));

		assertEquals(1, calls.get());
		assertSame(refusal, thrown);
	}

	@Test
	public void testExecute_connectionRefusedNestedInCause_isNotRetriedAndTheSameExceptionIsThrown()
	{
		AtomicInteger calls = new AtomicInteger();
		ConnectException refusal = new ConnectException("Connection refused");
		RuntimeException failure = new RuntimeException("journey failed",
			new RuntimeException("navigation failed", refusal));

		Exception thrown = assertThrows(Exception.class, () -> policy().execute(() ->
		{
			calls.incrementAndGet();
			throw failure;
		}));

		assertEquals(1, calls.get());
		assertSame(failure, thrown);
		assertSame(refusal, thrown.getCause().getCause());
	}

	@Test
	public void testExecute_checkedConnectExceptionThrownDirectly_runsOnceAndTheSameExceptionIsRethrown()
	{
		AtomicInteger calls = new AtomicInteger();
		ConnectException refusal = new ConnectException("Connection refused");

		Exception thrown = assertThrows(Exception.class, () -> policy().execute(() ->
		{
			calls.incrementAndGet();
			throw refusal;
		}));

		assertEquals(1, calls.get());
		assertSame(refusal, thrown);
	}

	@Test
	public void testExecute_refusalIsTheCauseOfAJourneyActionException_runsOnceAndTheSameExceptionIsRethrown()
	{
		AtomicInteger calls = new AtomicInteger();
		ConnectException refusal = new ConnectException("Connection refused");
		BaseJourneyActionException failure = new BaseJourneyActionException("action failed", null, refusal);

		Exception thrown = assertThrows(Exception.class, () -> policy().execute(() ->
		{
			calls.incrementAndGet();
			throw failure;
		}));

		assertEquals(1, calls.get());
		assertSame(failure, thrown);
		assertSame(refusal, thrown.getCause());
	}

	@Test
	public void testExecute_transientFailureThenARefusal_stopsAtTheRefusalAndThrowsIt()
	{
		AtomicInteger calls = new AtomicInteger();
		ConnectException refusal = new ConnectException("Connection refused");

		Exception thrown = assertThrows(Exception.class, () -> policy().execute(() ->
		{
			if(calls.incrementAndGet() == 1)
			{
				throw new IllegalStateException("flaky");
			}
			throw refusal;
		}));

		assertEquals(2, calls.get());
		assertSame(refusal, thrown);
	}

	@Test
	public void testExecute_nearMissesForARefusal_areRetriedUpToTheLimit()
	{
		assertEquals(4, callsBeforeGivingUp(policy(), new WebDriverException("unknown error: net::ERR_CONNECTION_RESET")));
		assertEquals(4, callsBeforeGivingUp(policy(), new ConnectException("Connection timed out: connect")));
		assertEquals(4, callsBeforeGivingUp(policy(),
			new WebDriverException("unknown error: net::ERR_CONNECTION_TIMED_OUT")));
		assertEquals(4, callsBeforeGivingUp(policy(),
			new WebDriverException("unknown error: net::ERR_NAME_NOT_RESOLVED")));
		assertEquals(4, callsBeforeGivingUp(policy(), new SocketTimeoutException("Read timed out")));
		assertEquals(4, callsBeforeGivingUp(policy(), new NoRouteToHostException("No route to host")));
	}

	@Test
	public void testExecute_refusalTextInAPageControlledException_isRetried()
	{
		assertEquals(4, callsBeforeGivingUp(policy(),
			new JavascriptException("unknown error: net::ERR_CONNECTION_REFUSED")));
		assertEquals(4, callsBeforeGivingUp(policy(),
			new UnhandledAlertException("unknown error: net::ERR_CONNECTION_REFUSED")));
	}

	private static int callsBeforeGivingUp(IRetryPolicy policy, Throwable failure)
	{
		AtomicInteger calls = new AtomicInteger();

		assertThrows(Exception.class, () -> policy.execute(() ->
		{
			calls.incrementAndGet();
			throw new RuntimeException("journey failed", failure);
		}));

		return calls.get();
	}

	private static RetryPolicyBuilder fastBuilder()
	{
		return RetryPolicyBuilder.builder().maxRetries(3).delay(Duration.ofMillis(1));
	}

	@Test
	public void testAbortOn_customRule_abortsOnACustomFailure()
	{
		IRetryPolicy policy = fastBuilder().abortOn(failure -> failure.getCause() instanceof IllegalArgumentException).build();

		assertEquals(1, callsBeforeGivingUp(policy, new IllegalArgumentException("bad input")));
		assertEquals(4, callsBeforeGivingUp(policy, new IllegalStateException("flaky")),
			"a failure no rule matches is still retried up to the limit");
	}

	@Test
	public void testAbortOn_calledMoreThanOnce_theRulesCombine()
	{
		IRetryPolicy policy = fastBuilder()
			.abortOn(failure -> failure.getCause() instanceof IllegalArgumentException)
			.abortOn(failure -> failure.getCause() instanceof UnsupportedOperationException)
			.build();

		assertEquals(1, callsBeforeGivingUp(policy, new IllegalArgumentException("bad input")));
		assertEquals(1, callsBeforeGivingUp(policy, new UnsupportedOperationException("nope")));
		assertEquals(4, callsBeforeGivingUp(policy, new IllegalStateException("flaky")));
	}

	@Test
	public void testAbortOn_customRule_defaultStillAbortsOnARefusal()
	{
		IRetryPolicy policy = fastBuilder()
			.abortOn(failure -> failure.getCause() instanceof IllegalArgumentException)
			.build();

		assertEquals(1, callsBeforeGivingUp(policy, new ConnectException("Connection refused")));
	}

	@Test
	public void testClearAbortRules_refusalIsRetriedUpToTheLimit()
	{
		IRetryPolicy policy = fastBuilder().clearAbortRules().build();

		assertEquals(4, callsBeforeGivingUp(policy, new ConnectException("Connection refused")));
	}

	@Test
	public void testClearAbortRules_ruleAddedAfterwardsStillApplies()
	{
		IRetryPolicy policy = fastBuilder()
			.clearAbortRules()
			.abortOn(failure -> failure.getCause() instanceof IllegalArgumentException)
			.build();

		assertEquals(1, callsBeforeGivingUp(policy, new IllegalArgumentException("bad input")));
		assertEquals(4, callsBeforeGivingUp(policy, new ConnectException("Connection refused")));
	}

	@Test
	public void testBuild_laterChangesToTheBuilderDoNotAffectAPolicyAlreadyBuilt()
	{
		RetryPolicyBuilder builder = fastBuilder();
		IRetryPolicy policy = builder.build();

		builder.clearAbortRules();

		assertEquals(1, callsBeforeGivingUp(policy, new ConnectException("Connection refused")));
	}

	@Test
	public void testAbortOn_null_isRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> RetryPolicyBuilder.builder().abortOn(null));
	}
}

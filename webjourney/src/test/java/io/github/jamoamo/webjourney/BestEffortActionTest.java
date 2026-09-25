/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.annotation.form.Element;
import io.github.jamoamo.webjourney.api.BestEffortDecision;
import io.github.jamoamo.webjourney.api.BestEffortOutcome;
import io.github.jamoamo.webjourney.api.CallableAction;
import io.github.jamoamo.webjourney.api.ConnectionFailures;
import io.github.jamoamo.webjourney.api.IBestEffortOutcomeListener;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IRetryPolicy;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.api.RetryPolicyBuilder;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import io.github.jamoamo.webjourney.api.web.XWebException;
import io.github.jamoamo.webjourney.reserved.JourneyBreadcrumb;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.WebDriverException;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class BestEffortActionTest
{
	private static final String NAME = "login";
	private static final String MDC_KEY = "WebJourney.Action";

	@FunctionalInterface
	private interface FailableSubJourney
	{
		IJourney apply(IJourneyBuilder builder) throws JourneyException;
	}

	private IJourneyContext newContext(IBrowserWindow mockWindow)
	{
		return newContext(mockWindow, RetryPolicyBuilder.builder().maxRetries(3).delay(Duration.ofMillis(1)).build());
	}

	private IJourneyContext newContext(IBrowserWindow mockWindow, IRetryPolicy retryPolicy)
	{
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);

		ITravelOptions options = Mockito.mock(ITravelOptions.class);
		Mockito.when(options.getRetryPolicy()).thenReturn(retryPolicy);

		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		Mockito.when(context.getJourneyBreadcrumb()).thenReturn(new JourneyBreadcrumb());
		Mockito.when(context.getOptions()).thenReturn(options);
		return context;
	}

	/**
	 * A best-effort action with no waits before or after it, so the tests do not sit through the default second.
	 */
	private BestEffortAction newAction(FailableSubJourney subJourney, IBestEffortOutcomeListener listener)
	{
		BestEffortAction action = new BestEffortAction(NAME, subJourney::apply, listener);
		action.setPreActionWait(0);
		action.setPostActionWait(0);
		return action;
	}

	/**
	 * A listener that records the outcomes it is given, and then carries on.
	 */
	private static IBestEffortOutcomeListener recordingTo(List<BestEffortOutcome> outcomes)
	{
		return outcome ->
		{
			outcomes.add(outcome);
			return BestEffortDecision.CONTINUE;
		};
	}

	/**
	 * A sub journey that navigates to the URL, without the default waits before and after the navigation.
	 */
	private static IJourney navigateWithoutWaits(IJourneyBuilder builder, String url) throws JourneyException
	{
		return builder.navigateTo(url)
			.withPreActionWait(0)
			.withPostActionWait(0)
			.build();
	}

	private static void assertNavigatedTo(IBrowserWindow window, String url) throws XNavigationError, XWebException
	{
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(window).navigateToUrl(urlCaptor.capture());
		assertEquals(url, urlCaptor.getValue().toString());
	}

	private static JourneyException newFailure()
	{
		return new JourneyException("could not build sub journey", new JourneyBreadcrumb());
	}

	@Test
	public void testExecuteActionImpl_subJourneySucceeds_runsItAndListenerReceivesSuccess()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.ifcalled.com"), recordingTo(outcomes));

		ActionResult result = instance.executeActionImpl(context);

		assertNavigatedTo(mockWindow, "https://www.ifcalled.com");
		assertEquals(ActionResult.SUCCESS, result);
		assertEquals(1, outcomes.size());
		BestEffortOutcome outcome = outcomes.get(0);
		assertTrue(outcome.isSuccess());
		assertNull(outcome.getFailure());
		assertEquals(NAME, outcome.getName());
		assertFalse(outcome.getElapsed().isNegative());
		assertEquals(0, outcome.getRetryCount());
	}

	@Test
	public void testExecuteActionImpl_subJourneySupplierThrows_listenerReceivesFailureAndItIsSwallowed()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		List<BestEffortOutcome> outcomes = new ArrayList<>();
		JourneyException failure = newFailure();

		BestEffortAction instance = newAction(builder ->
		{
			throw failure;
		}, recordingTo(outcomes));

		ActionResult result = instance.executeActionImpl(context);

		assertEquals(ActionResult.SUCCESS, result);
		assertEquals(1, outcomes.size());
		assertFalse(outcomes.get(0).isSuccess());
		assertSame(failure, outcomes.get(0).getFailure());
	}

	@Test
	public void testExecuteActionImpl_subJourneyConnectionRefused_listenerGetsOriginalCauseChainAndItIsNotRetried()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		ConnectException refusal = new ConnectException("Connection refused");
		Mockito.doThrow(new RuntimeException("navigation failed", refusal))
			.when(mockWindow)
			.navigateToUrl(Mockito.any());
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.refused.com"), recordingTo(outcomes));

		ActionResult result = instance.executeActionImpl(context);

		assertEquals(ActionResult.SUCCESS, result);
		assertEquals(1, outcomes.size());
		Throwable failure = outcomes.get(0).getFailure();
		assertNotNull(failure);
		assertTrue(ConnectionFailures.isConnectionRefused(failure));
		assertTrue(ExceptionUtils.getThrowableList(failure).contains(refusal),
			"the original ConnectException should be in the cause chain");
		Mockito.verify(mockWindow, Mockito.times(1)).navigateToUrl(Mockito.any());
		assertEquals(0, outcomes.get(0).getRetryCount());
	}

	@Test
	public void testExecuteActionImpl_subJourneyActionsRetried_outcomeCountsTheRetries()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		RuntimeException flaky = new IllegalStateException("flaky");
		Mockito.doThrow(flaky).doThrow(flaky).doReturn(null)
			.when(mockWindow)
			.navigateToUrl(Mockito.any());
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.flaky.com"), recordingTo(outcomes));

		instance.executeActionImpl(context);

		assertEquals(1, outcomes.size());
		assertTrue(outcomes.get(0).isSuccess());
		assertEquals(2, outcomes.get(0).getRetryCount());
		Mockito.verify(mockWindow, Mockito.times(3)).navigateToUrl(Mockito.any());
	}

	@Test
	public void testExecuteAction_abortAfterFailure_rethrowsOriginalFailureWithChainIntact()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		JourneyException failure = newFailure();

		BestEffortAction instance = newAction(builder ->
		{
			throw failure;
		}, outcome -> BestEffortDecision.ABORT);

		BaseJourneyActionException thrown = assertThrows(BaseJourneyActionException.class,
			() -> instance.executeAction(context));

		assertTrue(ExceptionUtils.getThrowableList(thrown).contains(failure),
			"the original failure should be in the cause chain, not replaced by a copy");
	}

	@Test
	public void testExecuteAction_abortAfterFailure_subJourneyAndListenerRunOnce()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AtomicInteger subJourneyRuns = new AtomicInteger();
		AtomicInteger listenerCalls = new AtomicInteger();

		BestEffortAction instance = newAction(builder ->
		{
			subJourneyRuns.incrementAndGet();
			throw newFailure();
		}, outcome ->
		{
			listenerCalls.incrementAndGet();
			return BestEffortDecision.ABORT;
		});

		assertThrows(BaseJourneyActionException.class, () -> instance.executeAction(context));

		assertEquals(1, subJourneyRuns.get(), "the sub journey must not be re-run by the retry policy");
		assertEquals(1, listenerCalls.get(), "the listener must not be called again by the retry policy");
	}

	@Test
	public void testExecuteAction_abortAfterFailure_isNotRetriedEvenByAPolicyThatRetriesEverything()
	{
		IRetryPolicy retryEverything = new IRetryPolicy()
		{
			@Override
			public <T> T execute(CallableAction<T> action) throws Exception
			{
				Exception last = null;
				for(int attempt = 0; attempt < 5; attempt++)
				{
					try
					{
						return action.call();
					}
					catch (Exception ex)
					{
						last = ex;
					}
				}
				throw last;
			}
		};
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class), retryEverything);
		AtomicInteger subJourneyRuns = new AtomicInteger();

		BestEffortAction instance = newAction(builder ->
		{
			subJourneyRuns.incrementAndGet();
			throw newFailure();
		}, outcome -> BestEffortDecision.ABORT);

		assertThrows(BaseJourneyActionException.class, () -> instance.executeAction(context));

		assertEquals(1, subJourneyRuns.get());
	}

	@Test
	public void testExecuteActionImpl_abortAfterSuccess_isIgnored()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));

		BestEffortAction instance = newAction(builder -> builder.build(), outcome -> BestEffortDecision.ABORT);

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
	}

	@Test
	public void testExecuteActionImpl_continueAfterFailure_returnsSuccess()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));

		BestEffortAction instance = newAction(builder ->
		{
			throw newFailure();
		}, outcome -> BestEffortDecision.CONTINUE);

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
	}

	@Test
	public void testExecuteActionImpl_listenerThrowsAfterSuccess_isCalledOnceSwallowedAndTreatedAsContinue()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AtomicInteger listenerCalls = new AtomicInteger();

		BestEffortAction instance = newAction(builder -> builder.build(), outcome ->
		{
			listenerCalls.incrementAndGet();
			throw new IllegalStateException("listener broke");
		});

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
		assertEquals(1, listenerCalls.get());
	}

	@Test
	public void testExecuteActionImpl_listenerThrowsAfterFailure_isCalledOnceSwallowedAndTreatedAsContinue()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AtomicInteger listenerCalls = new AtomicInteger();
		AtomicReference<BestEffortOutcome> received = new AtomicReference<>();

		BestEffortAction instance = newAction(builder ->
		{
			throw newFailure();
		}, outcome ->
		{
			listenerCalls.incrementAndGet();
			received.set(outcome);
			throw new IllegalStateException("listener broke");
		});

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
		assertEquals(1, listenerCalls.get());
		assertFalse(received.get().isSuccess(), "the listener that threw was given the failed outcome");
	}

	@Test
	public void testExecuteAction_listenerThrowsACheckedExceptionSneakily_isSwallowedAndNothingIsRetried()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AtomicInteger subJourneyRuns = new AtomicInteger();
		AtomicInteger listenerCalls = new AtomicInteger();

		BestEffortAction instance = newAction(builder ->
		{
			subJourneyRuns.incrementAndGet();
			throw newFailure();
		}, outcome ->
		{
			listenerCalls.incrementAndGet();
			throw BestEffortActionTest.<RuntimeException>sneakyThrow(new IOException("checked and sneaky"));
		});

		assertEquals(ActionResult.SUCCESS, instance.executeAction(context));
		assertEquals(1, subJourneyRuns.get(), "the sub journey must not be re-run because the listener threw");
		assertEquals(1, listenerCalls.get());
	}

	@Test
	public void testExecuteActionImpl_listenerThrowsAnError_propagates()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AssertionError error = new AssertionError("listener error");

		BestEffortAction instance = newAction(builder -> builder.build(), outcome ->
		{
			throw error;
		});

		assertSame(error, assertThrows(AssertionError.class, () -> instance.executeActionImpl(context)));
	}

	@Test
	public void testExecuteActionImpl_subJourneyThrowsFromDoJourney_listenerGetsTheExactException()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		List<BestEffortOutcome> outcomes = new ArrayList<>();
		JourneyException failure = newFailure();

		BestEffortAction instance = newAction(builder -> ctx ->
		{
			throw failure;
		}, recordingTo(outcomes));

		instance.executeActionImpl(context);

		assertEquals(1, outcomes.size());
		assertSame(failure, outcomes.get(0).getFailure());
	}

	@Test
	public void testExecuteActionImpl_subJourneyReturnsNoJourney_isAFailureWithAClearMessage()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(builder -> null, recordingTo(outcomes));

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
		assertEquals(1, outcomes.size());
		assertFalse(outcomes.get(0).isSuccess());
		assertTrue(outcomes.get(0).getFailure().getMessage().contains("returned no journey"));
		assertTrue(outcomes.get(0).getFailure().getMessage().contains(NAME));
	}

	@Test
	public void testExecuteActionImpl_chromiumRefusalWithoutAConnectExceptionCause_isNotRetriedAndIsARefusal()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		Mockito.doThrow(new WebDriverException("unknown error: net::ERR_CONNECTION_REFUSED"))
			.when(mockWindow)
			.navigateToUrl(Mockito.any());
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.refused.com"), recordingTo(outcomes));

		instance.executeActionImpl(context);

		Mockito.verify(mockWindow, Mockito.times(1)).navigateToUrl(Mockito.any());
		assertEquals(1, outcomes.size());
		assertTrue(ConnectionFailures.isConnectionRefused(outcomes.get(0).getFailure()));
		assertEquals(0, outcomes.get(0).getRetryCount());
	}

	@Test
	public void testExecuteActionImpl_nonRefusalRetriedUntilRetriesRunOut_listenerIsToldOnceWithAFailure()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		Mockito.doThrow(new IllegalStateException("always broken"))
			.when(mockWindow)
			.navigateToUrl(Mockito.any());
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.broken.com"), recordingTo(outcomes));

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));

		Mockito.verify(mockWindow, Mockito.times(4)).navigateToUrl(Mockito.any());
		assertEquals(1, outcomes.size());
		assertFalse(outcomes.get(0).isSuccess());
		assertEquals(3, outcomes.get(0).getRetryCount());
	}

	@Test
	public void testExecuteActionImpl_refusalInsideForEachChildElement_reachesTheListenerAsARefusalAndIsNotRerun()
		throws XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IWebPage page = mockPageWhoseChildrenFailWithARefusal(mockWindow);
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		BestEffortAction instance = newAction(BestEffortActionTest::repeatOverRows, recordingTo(outcomes));

		instance.executeActionImpl(context);

		assertEquals(1, outcomes.size());
		assertFalse(outcomes.get(0).isSuccess());
		assertTrue(ConnectionFailures.isConnectionRefused(outcomes.get(0).getFailure()),
			"the refusal inside the repeat must not lose its cause");
		Mockito.verify(page, Mockito.times(1)).getElement("rows");
	}

	@Test
	public void testExecuteAction_refusalInsideForEachChildElementAndAbort_stopsWithTheRefusalAndTheRepeatIsNotRerun()
		throws XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IWebPage page = mockPageWhoseChildrenFailWithARefusal(mockWindow);
		IJourneyContext context = newContext(mockWindow);
		AtomicInteger listenerCalls = new AtomicInteger();

		BestEffortAction instance = newAction(BestEffortActionTest::repeatOverRows, outcome ->
		{
			listenerCalls.incrementAndGet();
			return ConnectionFailures.isConnectionRefused(outcome.getFailure())
				? BestEffortDecision.ABORT
				: BestEffortDecision.CONTINUE;
		});

		BaseJourneyActionException thrown = assertThrows(BaseJourneyActionException.class,
			() -> instance.executeAction(context));

		assertTrue(ConnectionFailures.isConnectionRefused(thrown));
		assertEquals(1, listenerCalls.get());
		Mockito.verify(page, Mockito.times(1)).getElement("rows");
	}

	@Test
	public void testBestEffortJourney_listenerThrows_laterStepsStillRun()
		throws JourneyException, XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);
		AtomicInteger listenerCalls = new AtomicInteger();

		IJourney journey = JourneyBuilder.path()
			.bestEffortJourney(builder ->
			{
				throw newFailure();
			}, outcome ->
			{
				listenerCalls.incrementAndGet();
				throw new IllegalStateException("listener broke");
			})
			.navigateTo("https://www.after.com")
			.withPreActionWait(0)
			.withPostActionWait(0)
			.build();

		journey.doJourney(context);

		assertEquals(1, listenerCalls.get());
		assertNavigatedTo(mockWindow, "https://www.after.com");
	}

	private static IWebPage mockPageWhoseChildrenFailWithARefusal(IBrowserWindow mockWindow) throws XWebException
	{
		XWebException readFailure = new XWebException("could not read the rows");
		readFailure.initCause(new ConnectException("Connection refused"));
		AElement rows = Mockito.mock(AElement.class);
		Mockito.when(rows.getChildrenByTag(Mockito.any())).thenThrow(readFailure);
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("rows")).thenReturn(rows);
		Mockito.when(mockWindow.getCurrentPage()).thenReturn(page);
		return page;
	}

	private static IJourney repeatOverRows(IJourneyBuilder builder) throws JourneyException
	{
		ActionOptionsJourneyBuilder repeat = (ActionOptionsJourneyBuilder) ((BaseJourneyBuilder) builder)
			.forEachChildElement(RowsPage.class, "rows", null, JourneyBuilder.path().build());
		return repeat.withPreActionWait(0).withPostActionWait(0).build();
	}

	/**
	 * A page with one element to repeat over.
	 */
	private static final class RowsPage
	{
		@Element(xPath = "//rows")
		private Object rows;
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> RuntimeException sneakyThrow(Throwable throwable) throws E
	{
		throw (E) throwable;
	}

	@Test
	public void testExecuteActionImpl_listenerReturnsNull_isTreatedAsContinue()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));

		BestEffortAction instance = newAction(builder ->
		{
			throw newFailure();
		}, outcome -> null);

		assertEquals(ActionResult.SUCCESS, instance.executeActionImpl(context));
	}

	@Test
	public void testExecuteAction_listenerRunsUnderTheStepsMdcLabel()
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		AtomicReference<String> labelInListener = new AtomicReference<>();

		// the sub journey runs an action of its own, which must not strip the label from the enclosing step
		BestEffortAction instance = newAction(
			builder -> navigateWithoutWaits(builder, "https://www.label.com"),
			outcome ->
			{
				labelInListener.set(MDC.get(MDC_KEY));
				return BestEffortDecision.CONTINUE;
			});

		instance.executeAction(context);

		assertEquals(NAME, labelInListener.get());
		assertNull(MDC.get(MDC_KEY), "the label is removed once the outermost action has finished");
	}

	@Test
	public void testConstructor_nullListener_isRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> new BestEffortAction(NAME, builder -> builder.build(), null));
	}

	@Test
	public void testConstructor_nullOrBlankName_isRejected()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new BestEffortAction(null, builder -> builder.build(), BestEffortAction.NO_OP));
		assertThrows(IllegalArgumentException.class,
			() -> new BestEffortAction("  ", builder -> builder.build(), BestEffortAction.NO_OP));
	}

	@Test
	public void testNoOp_alwaysContinues()
	{
		BestEffortOutcome failed = BestEffortOutcome.failure(NAME, new IllegalStateException("broken"), Duration.ZERO, 0);

		assertEquals(BestEffortDecision.CONTINUE, BestEffortAction.NO_OP.onOutcome(failed));
	}

	@Test
	public void testBestEffortJourney_named_listenerIsToldTheNameAndJourneyContinues()
		throws JourneyException, XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);
		List<BestEffortOutcome> outcomes = new ArrayList<>();
		JourneyException failure = newFailure();

		IJourney journey = JourneyBuilder.path()
			.bestEffortJourney("sign in", builder ->
			{
				throw failure;
			}, recordingTo(outcomes))
			.navigateTo("https://www.after.com")
			.withPreActionWait(0)
			.withPostActionWait(0)
			.build();

		journey.doJourney(context);

		assertEquals(1, outcomes.size());
		assertEquals("sign in", outcomes.get(0).getName());
		assertSame(failure, outcomes.get(0).getFailure());
		assertNavigatedTo(mockWindow, "https://www.after.com");
	}

	@Test
	public void testBestEffortJourney_withoutListener_failureIsSwallowedAndJourneyContinues()
		throws JourneyException, XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);

		IJourney journey = JourneyBuilder.path()
			.bestEffortJourney(builder ->
			{
				throw newFailure();
			})
			.navigateTo("https://www.after.com")
			.withPreActionWait(0)
			.withPostActionWait(0)
			.build();

		journey.doJourney(context);

		assertNavigatedTo(mockWindow, "https://www.after.com");
	}

	@Test
	public void testBestEffortJourney_listenerOverloadWithoutName_usesTheDefaultName()
		throws JourneyException
	{
		IJourneyContext context = newContext(Mockito.mock(IBrowserWindow.class));
		List<BestEffortOutcome> outcomes = new ArrayList<>();

		IJourney journey = JourneyBuilder.path()
			.bestEffortJourney(builder -> builder.build(), recordingTo(outcomes))
			.build();

		journey.doJourney(context);

		assertEquals(1, outcomes.size());
		assertEquals("BestEffort", outcomes.get(0).getName());
	}

	@Test
	public void testBestEffortJourney_abort_stopsTheJourneyWithTheOriginalFailure()
		throws JourneyException, XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);
		JourneyException failure = newFailure();

		IJourney journey = JourneyBuilder.path()
			.bestEffortJourney(builder ->
			{
				throw failure;
			}, outcome -> BestEffortDecision.ABORT)
			.navigateTo("https://www.after.com")
			.withPreActionWait(0)
			.withPostActionWait(0)
			.build();

		JourneyException thrown = assertThrows(JourneyException.class, () -> journey.doJourney(context));

		assertTrue(ExceptionUtils.getThrowableList(thrown).contains(failure));
		Mockito.verify(mockWindow, Mockito.never()).navigateToUrl(Mockito.any());
	}

	@Test
	public void testBestEffortJourney_nullListener_isRejectedAtBuildTime()
	{
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(builder -> builder.build(), null));

		assertEquals("outcomeListener cannot be null", thrown.getMessage());
	}

	@Test
	public void testBestEffortJourney_nullSubJourney_isRejectedAtBuildTime()
	{
		IllegalArgumentException withoutListener = assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(null));
		IllegalArgumentException withListener = assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(null, BestEffortAction.NO_OP));
		IllegalArgumentException named = assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(NAME, null, BestEffortAction.NO_OP));

		assertEquals("Sub Journey cannot be null", withoutListener.getMessage());
		assertEquals("Sub Journey cannot be null", withListener.getMessage());
		assertEquals("Sub Journey cannot be null", named.getMessage());
	}

	@Test
	public void testBestEffortJourney_nullOrBlankName_isRejected()
	{
		assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(null, builder -> builder.build(), BestEffortAction.NO_OP));
		assertThrows(IllegalArgumentException.class,
			() -> JourneyBuilder.path().bestEffortJourney(" ", builder -> builder.build(), BestEffortAction.NO_OP));
	}

	@Test
	public void testGetActionName_isTheStepsName()
	{
		BestEffortAction unnamed = new BestEffortAction(
			BestEffortAction.DEFAULT_NAME, builder -> builder.build(), BestEffortAction.NO_OP);

		assertEquals("BestEffort", unnamed.getActionName());
		assertEquals(NAME, newAction(builder -> builder.build(), BestEffortAction.NO_OP).getActionName());
	}
}

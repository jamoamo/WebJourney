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

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.BestEffortDecision;
import io.github.jamoamo.webjourney.api.BestEffortOutcome;
import io.github.jamoamo.webjourney.api.ConnectionFailures;
import io.github.jamoamo.webjourney.api.IBestEffortOutcomeListener;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.NonRetryableActionException;
import java.time.Duration;
import org.apache.commons.lang3.function.FailableFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sub-journey that is always attempted, but whose failure does not abort the rest of the journey --
 * unlike {@link ConditionalAction}, which rethrows any exception from its sub-journey. Intended for
 * steps that are worth attempting on every run (e.g. logging in) but whose own unreliability shouldn't
 * take down journeys that don't strictly depend on them succeeding.
 * <p>
 * An {@link IBestEffortOutcomeListener} is told how the sub-journey ended and decides what happens next: after a
 * failure, {@link BestEffortDecision#ABORT} stops the journey by rethrowing the original failure (cause chain intact),
 * without the step, and so the sub-journey, being retried. {@code ABORT} after a success is ignored. A listener that
 * throws an {@link Exception}, or returns null, is treated as {@link BestEffortDecision#CONTINUE}; an {@link Error}
 * propagates. A sub-journey supplier that returns no journey is a failure of the step like any other.
 * <p>
 * The step has a name, used in its log messages, as its action name and in the outcome the listener is given.
 *
 * @author James Amoore
 */
final class BestEffortAction extends AWebAction
{
	/**
	 * The name of a best-effort step that wasn't given one.
	 */
	static final String DEFAULT_NAME = "BestEffort";

	/**
	 * The listener used when the caller doesn't supply one: it never asks for the journey to stop.
	 */
	static final IBestEffortOutcomeListener NO_OP = outcome -> BestEffortDecision.CONTINUE;

	private static final Logger LOGGER = LoggerFactory.getLogger(BestEffortAction.class);

	private final String name;
	private final FailableFunction<IJourneyBuilder, IJourney, ? extends JourneyException> subJourneySupplier;
	private final IBestEffortOutcomeListener outcomeListener;

	/**
	 * Creates the action.
	 *
	 * @param name the name of the step.
	 * @param subJourneySupplier provides the sub-journey to attempt.
	 * @param outcomeListener told how the sub-journey ended, and decides whether the journey carries on.
	 * @throws IllegalArgumentException if the name is null or blank, or the sub journey supplier or listener is null.
	 */
	BestEffortAction(String name,
		FailableFunction<IJourneyBuilder, IJourney, ? extends JourneyException> subJourneySupplier,
		IBestEffortOutcomeListener outcomeListener)
	{
		if(name == null || name.isBlank())
		{
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		if(subJourneySupplier == null)
		{
			throw new IllegalArgumentException("Sub Journey cannot be null");
		}
		if(outcomeListener == null)
		{
			throw new IllegalArgumentException("outcomeListener cannot be null");
		}
		this.name = name;
		this.subJourneySupplier = subJourneySupplier;
		this.outcomeListener = outcomeListener;
	}

	@Override
	@SuppressWarnings("IllegalCatch")
	protected ActionResult executeActionImpl(IJourneyContext context)
	{
		RetryCountingContext countingContext = new RetryCountingContext(context);
		Exception failure = null;
		long startNanos = System.nanoTime();
		try
		{
			BaseJourneyBuilder builder = JourneyBuilder.path();
			IJourney journey = this.subJourneySupplier.apply(builder);
			if(journey == null)
			{
				throw new IllegalStateException(
					"The sub journey supplier for best-effort step '" + this.name + "' returned no journey");
			}
			journey.doJourney(countingContext);
		}
		catch (Exception ex)
		{
			failure = ex;
		}
		Duration elapsed = Duration.ofNanos(System.nanoTime() - startNanos);

		BestEffortOutcome outcome;
		if(failure == null)
		{
			outcome = BestEffortOutcome.success(this.name, elapsed, countingContext.getRetryCount());
			LOGGER.debug("Best-effort sub journey '{}' succeeded in {} ms ({} retries).",
				this.name, elapsed.toMillis(), countingContext.getRetryCount());
		}
		else
		{
			outcome = BestEffortOutcome.failure(this.name, failure, elapsed, countingContext.getRetryCount());
			if(ConnectionFailures.isConnectionRefused(failure))
			{
				LOGGER.warn("Best-effort sub journey '{}' failed: the connection was refused.",
					this.name, failure);
			}
			else
			{
				LOGGER.warn("Best-effort sub journey '{}' failed.", this.name, failure);
			}
		}

		if(notifyListener(outcome) == BestEffortDecision.ABORT && failure != null)
		{
			// not retried: AWebAction unwraps this and rethrows the original failure once the retry policy is done
			throw new NonRetryableActionException(failure);
		}
		return ActionResult.SUCCESS;
	}

	private BestEffortDecision notifyListener(BestEffortOutcome outcome)
	{
		try
		{
			BestEffortDecision decision = this.outcomeListener.onOutcome(outcome);
			return decision == null ? BestEffortDecision.CONTINUE : decision;
		}
		catch (Exception ex)
		{
			LOGGER.error("Best-effort outcome listener for '{}' failed and could not act on {}; continuing with the rest "
				+ "of the journey.", this.name, outcome, ex);
			return BestEffortDecision.CONTINUE;
		}
	}

	@Override
	protected String getActionName()
	{
		return this.name;
	}
}

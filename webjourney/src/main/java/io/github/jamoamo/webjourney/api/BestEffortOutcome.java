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

import java.time.Duration;

/**
 * How a best-effort sub journey ended: either it completed, or it failed with the exception that was swallowed. Also
 * carries the step's name, how long the sub journey took and how many times actions within it were retried. Immutable.
 *
 * @see IJourneyBuilder#bestEffortJourney(String, org.apache.commons.lang3.function.FailableFunction, IBestEffortOutcomeListener)
 * @author James Amoore
 */
public final class BestEffortOutcome
{
	private final String name;
	private final Throwable failure;
	private final Duration elapsed;
	private final int retryCount;

	private BestEffortOutcome(String name, Throwable failure, Duration elapsed, int retryCount)
	{
		if(name == null || name.isBlank())
		{
			throw new IllegalArgumentException("name cannot be null or blank");
		}
		if(elapsed == null || elapsed.isNegative())
		{
			throw new IllegalArgumentException("elapsed cannot be null or negative");
		}
		if(retryCount < 0)
		{
			throw new IllegalArgumentException("retryCount cannot be negative");
		}
		this.name = name;
		this.failure = failure;
		this.elapsed = elapsed;
		this.retryCount = retryCount;
	}

	/**
	 * The outcome of a sub journey that completed.
	 *
	 * @param name the name of the best-effort step
	 * @param elapsed how long the sub journey took
	 * @param retryCount how many times actions within the sub journey were retried
	 * @return a successful outcome
	 */
	public static BestEffortOutcome success(String name, Duration elapsed, int retryCount)
	{
		return new BestEffortOutcome(name, null, elapsed, retryCount);
	}

	/**
	 * The outcome of a sub journey that failed.
	 *
	 * @param name the name of the best-effort step
	 * @param failure the exception the sub journey failed with
	 * @param elapsed how long the sub journey took before it failed
	 * @param retryCount how many times actions within the sub journey were retried
	 * @return a failed outcome
	 */
	public static BestEffortOutcome failure(String name, Throwable failure, Duration elapsed, int retryCount)
	{
		if(failure == null)
		{
			throw new IllegalArgumentException("failure cannot be null");
		}
		return new BestEffortOutcome(name, failure, elapsed, retryCount);
	}

	/**
	 * The name of the best-effort step this is the outcome of.
	 *
	 * @return the step's name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Did the sub journey complete?
	 *
	 * @return true if the sub journey completed, false if it failed
	 */
	public boolean isSuccess()
	{
		return this.failure == null;
	}

	/**
	 * The exception that made the sub journey fail. It is non-null exactly when {@link #isSuccess()} is false.
	 * <p>
	 * This is the original, unwrapped exception, so the failure of interest (e.g. a refused connection) may be any
	 * exception in its cause chain: inspect the whole chain, for example with
	 * {@link ConnectionFailures#isConnectionRefused(Throwable)}, rather than just the top exception.
	 * <p>
	 * It may reference the failed action and its inputs, so do not retain the outcome beyond the listener callback.
	 * Exceptions such as {@code BaseJourneyActionException} expose the action, and messages can include text taken from
	 * the page being visited, so treat the failure as untrusted and sensitive: do not forward it to a user or another
	 * system unfiltered.
	 *
	 * @return the failure, or null if the sub journey completed
	 */
	public Throwable getFailure()
	{
		return this.failure;
	}

	/**
	 * How long the sub journey ran for, including any waits and retries within it, up to when it completed or failed.
	 *
	 * @return the elapsed time
	 */
	public Duration getElapsed()
	{
		return this.elapsed;
	}

	/**
	 * How many times an action within the sub journey was retried, summed over all of its actions. Zero if nothing
	 * needed retrying. Only counts retries made by the journey's retry policy for actions run in the sub journey, not
	 * retries of the best-effort step itself.
	 *
	 * @return the number of retries
	 */
	public int getRetryCount()
	{
		return this.retryCount;
	}

	@Override
	public String toString()
	{
		return "BestEffortOutcome[name=" + this.name
			+ (isSuccess() ? ", success" : ", failure=" + this.failure)
			+ ", elapsed=" + this.elapsed.toMillis() + "ms, retries=" + this.retryCount + "]";
	}
}

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

/**
 * A listener notified of how a best-effort sub journey ended, which can also decide whether the journey carries on.
 *
 * @see IJourneyBuilder#bestEffortJourney(org.apache.commons.lang3.function.FailableFunction, IBestEffortOutcomeListener)
 * @author James Amoore
 */
@FunctionalInterface
public interface IBestEffortOutcomeListener
{
	/**
	 * Notifies the listener of how the sub journey ended, and asks what to do next. Called once each time the
	 * best-effort step runs, after any retries within the sub journey have finished.
	 * <ul>
	 * <li>{@link BestEffortDecision#CONTINUE} (or null) carries on with the rest of the journey.</li>
	 * <li>{@link BestEffortDecision#ABORT} after a failed outcome stops the journey with the original failure. The
	 * best-effort step is not retried and this method is not called again. After a successful outcome it is ignored.</li>
	 * <li>Any {@link Exception} thrown from here (checked or not) is logged and ignored, and is treated as
	 * {@code CONTINUE}. An {@link Error} is not caught: it propagates.</li>
	 * </ul>
	 *
	 * @param outcome how the sub journey ended. Do not retain it beyond this call.
	 * @return what to do next.
	 */
	BestEffortDecision onOutcome(BestEffortOutcome outcome);
}

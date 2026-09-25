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
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.api.entity.IEntityCreationListener;

/**
 * An observer of a journey. Gets notified of events occurring in the journey.
 * @author James Amoore
 */
public interface IJourneyObserver extends IEntityCreationListener
{
	/**
	 * notifies the observer that an action has started.
	 * @param action the action that started.
	 */
	void actionStarted(AWebAction action);
	
	/**
	 * notifies the observer that an action has ended.
	 * @param action the action that ended.
	 */
	void actionEnded(AWebAction action);

	/**
	 * notifies the observer that an attempt at an action failed and the action is about to be attempted again by the
	 * retry policy. Called when the next attempt starts. Does nothing unless overridden.
	 * @param action the action being retried.
	 * @param attempt the number of the attempt that failed, starting at 1.
	 * @param failure why that attempt failed.
	 */
	default void actionRetried(AWebAction action, int attempt, Throwable failure)
	{
	}

	/**
	 * notifies the observer that an attempt at an action failed and the retry policy did not retry it, either because
	 * a rule said not to (e.g. a refused connection) or because it ran out of retries. The action then fails with that
	 * failure. Does nothing unless overridden.
	 * @param action the action that failed.
	 * @param attempt the number of the attempt that failed, starting at 1, so also how many attempts were made.
	 * @param failure why that attempt failed.
	 */
	default void actionRetryAborted(AWebAction action, int attempt, Throwable failure)
	{
	}
}

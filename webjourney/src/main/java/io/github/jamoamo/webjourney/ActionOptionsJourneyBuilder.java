/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
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

import java.util.concurrent.TimeUnit;


/**
 * Builder for an action in a journey that includes options.
 * @author James Amoore
 */
@SuppressWarnings("abstractClassName")
public class ActionOptionsJourneyBuilder extends BaseJourneyBuilder
{
	/**
	 * Creates a new instance.
	 * @param build The Journey build that is in the progress of being built
	 */
	public ActionOptionsJourneyBuilder(JourneyBuild build)
	{
		super(build);
	}
	
	/**
	 * Sets the wait, in seconds, before the action. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait before the action is executed.
	 * @return this builder
	 */
	public ActionOptionsJourneyBuilder withPreActionWait(int waitTimeSeconds)
	{
		this.getBuild().getLastAction().setPreActionWait(TimeUnit.SECONDS.toMillis(waitTimeSeconds));
		return this;
	}
	
	/**
	 * Sets the wait, in seconds, before the action. Default wait is 1 second.
	 * @param waitTimeSeconds The number of seconds to wait before the action is executed.
	 * @return this builder
	 */
	public ActionOptionsJourneyBuilder withPostActionWait(int waitTimeSeconds)
	{
		this.getBuild().getLastAction().setPostActionWait(TimeUnit.SECONDS.toMillis(waitTimeSeconds));
		return this;
	}
}

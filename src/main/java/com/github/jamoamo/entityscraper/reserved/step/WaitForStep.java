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
package com.github.jamoamo.entityscraper.reserved.step;

import com.github.jamoamo.entityscraper.api.IJourneyContext;
import com.github.jamoamo.entityscraper.api.WaitFor;
import com.github.jamoamo.entityscraper.reserved.log.LogMessage;
import com.github.jamoamo.entityscraper.reserved.log.LogSeverity;

/**
 * A journey step to wait for a duration.
 * @author James Amoore
 */
public final class WaitForStep extends AJourneyStep
{
	private final WaitFor waitFor;
	
	/**
	 * Creates a new instance.
	 * @param waitFor The action.
	 */
	public WaitForStep(WaitFor waitFor)
	{
		this.waitFor = waitFor;
	}
	
	/**
	 * Executes the step.
	 * @param context The journey context
	 */
	@Override
	public void executeStep(IJourneyContext context)
	{
		context.log(
				  new LogMessage(
							 LogSeverity.INFO, 
							 "Waiting for [" + waitFor.getWaitDuration().toMillis() + "] milliseconds."));
		try
		{
			Thread.sleep(waitFor.getWaitDuration().toMillis());
		}
		catch(InterruptedException ex)
		{
			context.log(
					  new LogMessage(
								 LogSeverity.WARNING, 
								 "Wait For journey step was interrupted."));
		}
	}
}

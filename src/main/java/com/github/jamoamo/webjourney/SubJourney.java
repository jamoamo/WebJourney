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
package com.github.jamoamo.webjourney;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class SubJourney
{
	private final static Logger LOGGER = LoggerFactory.getLogger(WebTraveller.class);
	private final List<AWebAction> actions;

	SubJourney(List<AWebAction> actions)
	{
		this.actions = actions;
	}

	public void doJourney(IJourneyContext context)
			  throws JourneyException
	{
		this.actions.forEach(action ->
		{
			waitFor(action.getPreActionWaitTime());
			ActionResult result = action.executeAction(context);
			if(result == ActionResult.FAILURE)
			{
				throw new JourneyException("Action failed: " + action.getClass());
			}
			waitFor(action.getPostActionWaitTime());
		});
	}

	private void waitFor(long timeMillis)
	{
		try
		{
			Thread.sleep(timeMillis);
		}
		catch(InterruptedException ex)
		{
			LOGGER.info("Wait Interrupted");
		}
	}
}

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

import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;

/**
 * Abstract class for actions that can be performed on a web journey.
 * @author James Amoore
 */
public abstract class AWebAction
{
	private static final String ACTION_LOG_LABEL = "labels.WebJourney.Action";
	private static final int DEFAULT_WAIT_SEC = 1;
	private long preActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	private long postActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	
	protected abstract ActionResult executeActionImpl(IJourneyContext context)
		throws BaseJourneyActionException;
	
	protected abstract String getActionName();
	
	protected final ActionResult executeAction(IJourneyContext context)
			  throws BaseJourneyActionException
	{
		MDC.put(ACTION_LOG_LABEL, getActionName());
		
		try
		{
			return executeActionImpl(context);
		}
		finally
		{
			MDC.remove(ACTION_LOG_LABEL);
		}
	}

	/**
	 * Sets the wait to be performed prior to the action being performed.
	 * @param preActionWait The wait time in milliseconds.
	 */
	protected void setPreActionWait(long preActionWait)
	{
		this.preActionWait = preActionWait;
	}
	
	/**
	 * Sets the wait to be performed after the action has been performed.
	 * @param postActionWait The wait time in milliseconds.
	 */
	protected void setPostActionWait(long postActionWait)
	{
		this.postActionWait = postActionWait;
	}
	
	/**
	 * Retrieves the wait time to be performed prior to the action being performed.
	 * @return the wait time, in milliseconds.
	 */
	public long getPreActionWaitTime()
	{
		return this.preActionWait;
	}
	
	/**
	 * Retrieves the wait time to be performed after the action has been performed.
	 * @return the wait time, in milliseconds.
	 */
	public long getPostActionWaitTime()
	{
		return this.postActionWait;
	}
}

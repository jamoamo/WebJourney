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
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.ActionResult;
import io.github.jamoamo.webjourney.BaseJourneyActionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.MDC;

/**
 * Abstract class for actions that can be performed on a web journey.
 * @author James Amoore
 */
public abstract class AWebAction implements ICrumb
{
	private static final String ACTION_LOG_LABEL = "WebJourney.Action";
	private static final int DEFAULT_WAIT_SEC = 1;
	private long preActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	private long postActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	
	protected abstract ActionResult executeActionImpl(IJourneyContext context)
		throws BaseJourneyActionException;
	
	/**
	 * An identifier for the action. Will show in the journey breadcrumb.
	 * @return the action's name.
	 */
	protected abstract String getActionName();
	
	/**
	 * Executes the action.
	 * @param context the journey context
	 * @return the result of the action.
	 * @throws BaseJourneyActionException id something goes wrong.
	 */
	public final ActionResult executeAction(IJourneyContext context)
			  throws BaseJourneyActionException
	{
		MDC.put(ACTION_LOG_LABEL, getActionName());
		
		try
		{
			ActionRetryPolicy retryPolicy = context.getActionRetryPolicy();
			int attempt = 1;
			while(true)
			{
				try
				{
					return executeActionImpl(context);
				}
				catch(RuntimeException ex)
				{
					if(retryPolicy != null && retryPolicy.shouldRetry(attempt, ex))
					{
						attempt++;
						try
						{
							Thread.sleep(retryPolicy.getRetryDelay());
						}
						catch(InterruptedException ie)
						{
							Thread.currentThread().interrupt();
							throw new BaseJourneyActionException("Interrupted during retry wait", this, ie);
						}
					}
					else
					{
						if(ex instanceof BaseJourneyActionException)
						{
							throw (BaseJourneyActionException) ex;
						}
						else
						{
							throw (RuntimeException) ex;
						}
					}
				}
			}
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
	public void setPreActionWait(long preActionWait)
	{
		this.preActionWait = preActionWait;
	}
	
	/**
	 * Sets the wait to be performed after the action has been performed.
	 * @param postActionWait The wait time in milliseconds.
	 */
	public void setPostActionWait(long postActionWait)
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

	@Override
	public final String getCrumbType()
	{
		return "Action";
	}

	@Override
	public final String getCrumbName()
	{
		return getActionName();
	}
}

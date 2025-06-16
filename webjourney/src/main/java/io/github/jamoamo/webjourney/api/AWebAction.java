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
 * Abstract class for actions that can be performed on a web journey. Defines the common structure
 * and lifecycle for all web actions within a journey.
 * @author James Amoore
 * @see IJourney
 * @see IJourneyBuilder
 * @since 1.0.0
 */
public abstract class AWebAction implements ICrumb
{
	private static final String ACTION_LOG_LABEL = "WebJourney.Action";
	private static final int DEFAULT_WAIT_SEC = 1;
	private long preActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	private long postActionWait = TimeUnit.SECONDS.toMillis(DEFAULT_WAIT_SEC);
	
	/**
	 * Executes the specific implementation of the web action.
	 * Subclasses must provide their own implementation of this method.
	 * @param context The journey context, providing access to the browser and other journey-specific information.
	 * @return The result of the action execution.
	 * @throws BaseJourneyActionException if an error occurs during the execution of the action.
	 * @since 1.0.0
	 */
	protected abstract ActionResult executeActionImpl(IJourneyContext context)
		throws BaseJourneyActionException;
	
	/**
	 * An identifier for the action. This name will be displayed in the journey breadcrumb.
	 * @return The action's descriptive name.
	 * @since 1.0.0
	 */
	protected abstract String getActionName();
	
	/**
	 * Executes the action within the given journey context. This method handles logging and error wrapping,
	 * delegating the core action logic to {@link #executeActionImpl(IJourneyContext)}.
	 * @param context The journey context, providing access to the browser and other journey-specific information.
	 * @return The result of the action execution.
	 * @throws BaseJourneyActionException if something goes wrong during the action execution.
	 * @since 1.0.0
	 */
	public final ActionResult executeAction(IJourneyContext context)
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
	 * Sets the duration to wait in milliseconds before executing this action.
	 * @param preActionWait The wait time in milliseconds.
	 * @since 1.0.0
	 */
	public void setPreActionWait(long preActionWait)
	{
		this.preActionWait = preActionWait;
	}
	
	/**
	 * Sets the duration to wait in milliseconds after executing this action.
	 * @param postActionWait The wait time in milliseconds.
	 * @since 1.0.0
	 */
	public void setPostActionWait(long postActionWait)
	{
		this.postActionWait = postActionWait;
	}
	
	/**
	 * Retrieves the configured wait time in milliseconds to be performed prior to the action being executed.
	 * @return The wait time, in milliseconds.
	 * @since 1.0.0
	 */
	public long getPreActionWaitTime()
	{
		return this.preActionWait;
	}
	
	/**
	 * Retrieves the configured wait time in milliseconds to be performed after the action has been executed.
	 * @return The wait time, in milliseconds.
	 * @since 1.0.0
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

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

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IWebJourneyPath;
import io.github.jamoamo.webjourney.api.ICrumb;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class SubJourney implements IJourney, ICrumb
{
	private final static Logger LOGGER = LoggerFactory.getLogger(WebTraveller.class);
	private final List<AWebAction> actions;

	SubJourney(List<AWebAction> actions)
	{
		this.actions = actions;
	}

	SubJourney(IWebJourneyPath path)
	{
		this.actions = path.getActions();
	}

	@Override
	public void doJourney(final IJourneyContext context)
			  throws JourneyException
	{
		context.getJourneyBreadcrumb().pushCrumb(this);
		try
		{
			this.actions.stream().sequential().forEach(action ->
			{
				processAction(action, context);
			});
		}
		finally
		{
			context.getJourneyBreadcrumb().popCrumb();
		}
	}

	private void processAction(AWebAction action, IJourneyContext context)
			  throws JourneyException
	{
		context.getJourneyObservers().forEach(observer -> observer.actionStarted(action));
		context.getJourneyBreadcrumb().pushCrumb(action);
		try
		{
			waitFor(action.getPreActionWaitTime());

			try
			{
				ActionResult result = action.executeAction(context);
				if(result == ActionResult.FAILURE)
				{
					throw new JourneyException("Action failed: " + action.getClass(), context.getJourneyBreadcrumb());
				}
			}
			catch(JourneyException ex)
			{
				if(ex.getBreadcrumb() == null)
				{
					throw new JourneyException(ex.getMessage(), ex.getCause(), context.getJourneyBreadcrumb());
				}
				throw ex;
			}
			catch(Exception ex)
			{
				throw new JourneyException(ex, context.getJourneyBreadcrumb());
			}
			waitFor(action.getPostActionWaitTime());
		}
		finally
		{
			context.getJourneyBreadcrumb().popCrumb();
			context.getJourneyObservers().forEach(observer -> observer.actionEnded(action));
		}
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

	@Override
	public String getCrumbName()
	{
		return "SubJourney";
	}

	@Override
	public String getCrumbType()
	{
		return "Journey";
	}
}

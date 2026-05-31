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
import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.time.Duration;

/**
 * Action that blocks until the active window's url no longer contains a given substring. This is useful when a previous
 * action lands on an intermediate url that redirects to the desired page; waiting for the url to change away from the
 * intermediate url avoids acting on the wrong page.
 *
 * @author James Amoore
 */
class WaitUntilUrlChangesAction extends AWebAction
{
	private static final long POLL_INTERVAL_MILLIS = 250;

	private final String urlSubstring;
	private final Duration timeout;

	WaitUntilUrlChangesAction(String urlSubstring, Duration timeout)
	{
		if(urlSubstring == null)
		{
			throw new NullPointerException("Url substring should not be null.");
		}

		if(timeout == null)
		{
			throw new NullPointerException("Timeout should not be null.");
		}

		this.urlSubstring = urlSubstring;
		this.timeout = timeout;
	}

	@Override
	protected ActionResult executeActionImpl(IJourneyContext context)
			  throws BaseJourneyActionException
	{
		try
		{
			IBrowser browser = context.getBrowser();
			long deadline = System.currentTimeMillis() + this.timeout.toMillis();

			while(browser.getActiveWindow().getCurrentUrl().contains(this.urlSubstring))
			{
				if(System.currentTimeMillis() >= deadline)
				{
					throw new BaseJourneyActionException(
							  "Timed out after " + this.timeout.toMillis()
							  + "ms waiting for the url to change away from [" + this.urlSubstring + "].",
							  this, null);
				}

				Thread.sleep(POLL_INTERVAL_MILLIS);
			}
		}
		catch(InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw new BaseJourneyActionException(ex.getMessage(), this, ex);
		}
		catch(BaseJourneyActionException ex)
		{
			throw ex;
		}
		catch(Exception ex)
		{
			throw new BaseJourneyActionException(ex.getMessage(), this, ex);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	protected String getActionName()
	{
		return "Wait Until Url Changes";
	}
}

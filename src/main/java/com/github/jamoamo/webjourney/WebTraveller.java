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

import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A traveller of web journeys.
 * @author James Amoore
 */
public class WebTraveller
{
	private final Logger logger = LoggerFactory.getLogger(WebTraveller.class);
	
	private final TravelOptions travelOptions;
	
	/**
	 * Creates a new WebTraveller with the provided TravelOptions.
	 * @param options the options for the traveller.
	 */
	public WebTraveller(TravelOptions options)
	{
		this.travelOptions = options;
	}
	
	/**
	 * Travel the provided journey.
	 * @param journey the journey to travel.
	 */
	public void travelJourney(WebJourney journey)
	{
		this.logger.info("Starting Journey.");
		IPreferredBrowserStrategy browserStrategy = this.travelOptions.getPreferredBrowserStrategy();
		IBrowser browser = browserStrategy.getPreferredBrowser();
		JourneyContext context = new JourneyContext();
		context.setBrowser(browserStrategy.getPreferredBrowser());
		try
		{
			SubJourney subJourney = new SubJourney(journey.getActions());
			subJourney.doJourney(context);
		}
		catch(JourneyException ex)
		{
			this.logger.error("Can't complete journey: ", ex);
		}
		finally
		{
			browser.exit();
		}
	}
}

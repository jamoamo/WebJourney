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

import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserOptions;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.reserved.BreadcrumbPrinter;
import io.github.jamoamo.webjourney.reserved.JourneyBreadcrumb;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * A traveller of web journeys.
 * @author James Amoore
 */
public class WebTraveller
{
	private static final String LOGGER_CONTEXT_JOURNEY_LABEL = "label.WebJourney.id";
	private final Logger logger = LoggerFactory.getLogger(WebTraveller.class);
	
	private final ITravelOptions travelOptions;
	
	/**
	 * Creates a new WebTraveller with the provided TravelOptions.
	 * @param options the options for the traveller.
	 */
	public WebTraveller(ITravelOptions options)
	{
		this.travelOptions = options;
	}
	
	/**
	 * Travel the provided journey.
	 * @param journey the journey to travel.
	 */
	public void travelJourney(IJourney journey)
	{
		MDC.put(LOGGER_CONTEXT_JOURNEY_LABEL, UUID.randomUUID().toString());
		this.logger.info("Starting Journey.");
		IPreferredBrowserStrategy browserStrategy = this.travelOptions.getPreferredBrowserStrategy();
		
		// Create context first so browser arguments can be accessed during browser creation
		JourneyContext context = new JourneyContext();
		IJourneyBreadcrumb breadcrumb = new JourneyBreadcrumb();
		context.setJourneyBreadcrumb(breadcrumb);
		context.setJourneyObservers(this.travelOptions.getJourneyObservers());
		context.setActionRetryPolicy(this.travelOptions.getActionRetryPolicy());
		
		// Create browser with context for browser arguments
		IBrowser browser = browserStrategy.getPreferredBrowser(new DefaultBrowserOptions(), context);
		context.setBrowser(browser);
		try
		{
			journey.doJourney(context);
		}
		catch(JourneyException ex)
		{
			String breadcrumbString = getBreadcrumb(ex);
			this.logger.error("Can't complete journey (" + breadcrumbString + ": " + ex.getMessage());
			throw ex;
		}
		finally
		{
			if (browser != null) {
				browser.exit();
			}
			MDC.remove(LOGGER_CONTEXT_JOURNEY_LABEL);
		}
	}

	private String getBreadcrumb(JourneyException ex)
	{
		String breadcrumbString = "<unknown context>";
		if(ex.getBreadcrumb() != null)
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try
			{
				new BreadcrumbPrinter().printBreadCrumb(stream, ex.getBreadcrumb());
				breadcrumbString = stream.toString();
			}
			catch(IOException e)
			{
				//ignore exception.
			}
		}
		return breadcrumbString;
	}
}

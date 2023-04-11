/*
 * The MIT License
 *
 * Copyright 2023 amoor.
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
package com.github.jamoamo.entityscraper.api;

/**
 * The path that the journey should follow.
 * @author James Amoore
 */
public interface IJourneyPath
{
	/**
	 * Navigate to another page.
	 * @param navigate where to navigate to.
	 * @return the journey instance.
	 */
	IJourneyPath navigateTo(Navigate navigate);
	
	/**
	 * 
	 * @param <T> the type of the form to fill.
	 * @param formFill The form to fill
	 * @return the journey instance.
	 */
	<T> IJourneyPath fillForm(FormFill<T> formFill);
	
	/**
	 * Scrape a page.
	 * @param scrapePage The page to scrape
	 * @return the journey instance.
	 */
	IJourneyPath scrape(ScrapePage scrapePage);
	
	/**
	 * Dismiss a banner.
	 * @param click The banner to dismiss.
	 * @return the journey instance.
	 */
	IJourneyPath click(ButtonClick click);
	
	/**
	 * Wait for a set duration before continuing the journey.
	 * @param waitFor The wait duration
	 * @return the journey instance.
	 */
	IJourneyPath waitFor(WaitFor waitFor);
	
	/**
	 * Called to execute the steps in the path.
	 * @param context The context of the current journey.
	 */
	void followPath(IJourneyContext context);
}

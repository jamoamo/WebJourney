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

import com.github.jamoamo.entityscraper.reserved.step.RepeatedJourneyStep;
import com.github.jamoamo.entityscraper.reserved.step.FillFormStep;
import com.github.jamoamo.entityscraper.reserved.step.WaitForStep;
import com.github.jamoamo.entityscraper.reserved.step.ButtonClickStep;
import com.github.jamoamo.entityscraper.reserved.step.ScrapeStep;
import com.github.jamoamo.entityscraper.reserved.step.NavigateStep;
import com.github.jamoamo.entityscraper.reserved.step.AJourneyStep;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The path a journey takes.
 * @author James Amoore
 */
public class JourneyPath implements IJourneyPath
{
	private final Queue<AJourneyStep> steps;
	
	JourneyPath()
	{
		this.steps = new LinkedList<>();
	}
	
	/**
	 * Creates a new Web browsing journey.
	 * @return a Journey instance.
	 */
	public static JourneyPath start()
	{
		return new JourneyPath();
	}
	
	/**
	 * Navigate to another page.
	 * @param navigate where to navigate to.
	 * @return the journey instance.
	 */
	@Override
	public JourneyPath navigateTo(Navigate navigate)
	{
		this.steps.add(new NavigateStep(navigate));
		return this;
	}
	
	/**
	 * 
	 * @param <T> the type of the form to fill.
	 * @param formFill The form to fill
	 * @return the journey instance.
	 */
	@Override
	public <T> JourneyPath fillForm(FormFill<T> formFill)
	{
		this.steps.add(new FillFormStep(formFill));
		return this;
	}
	
	/**
	 * RepeatedPath a series of steps.
	 * @param <U> The repeat class.
	 * @param forEach The actions to repeat.
	 * @return the journey instance.
	 */
	public <U> JourneyPath repeat(RepeatedPath<U> forEach)
	{
		this.steps.add(new RepeatedJourneyStep(forEach));
		return this;
	}
	
	/**
	 * Wait for a set duration before continuing the journey.
	 * @param waitFor The wait duration
	 * @return the journey instance.
	 */
	public JourneyPath waitFor(WaitFor waitFor)
	{
		this.steps.add(new WaitForStep(waitFor));
		return this;
	}
	
	/**
	 * Scrape a page.
	 * @param scrapePage The page to scrape.
	 * @return The journey instance.
	 */
	@Override
	public JourneyPath scrape(ScrapePage scrapePage)
	{
		this.steps.add(new ScrapeStep(scrapePage));
		return this;
	}

	/**
	 * Clicks a button.
	 * @param click The banner to dismiss.
	 * @return the journey instance.
	 */
	@Override
	public JourneyPath click(ButtonClick click)
	{
		this.steps.add(new ButtonClickStep(click));
		return this;
	}

	/**
	 * Called to execute the steps in the path.
	 * @param context The context of the current journey.
	 */
	@Override
	public void followPath(IJourneyContext context)
	{
		for(AJourneyStep step : steps)
		{
			step.executeStep(context);
			context.waitForDefault();
		}
	}
}

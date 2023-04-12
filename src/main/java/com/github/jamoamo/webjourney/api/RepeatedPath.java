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
package com.github.jamoamo.webjourney.api;

import com.github.jamoamo.webjourney.reserved.step.ButtonClickStep;
import com.github.jamoamo.webjourney.reserved.step.FillFormStep;
import com.github.jamoamo.webjourney.reserved.step.NavigateStep;
import com.github.jamoamo.webjourney.reserved.step.ScrapeStep;
import com.github.jamoamo.webjourney.reserved.step.WaitForStep;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * RepeatedPath a series of actions.
 * @author James Amoore
 * @param <U> the input type for the repeat action.
 */
public final class RepeatedPath<U> implements IJourneyPath
{
	private final Stream<U> stream;
	
	private final Queue<DelayedStep<U>> journeySteps = new LinkedList<>();
	private long delay;
	
	RepeatedPath(Stream<U> stream)
	{
		this.stream = stream;
	}
	
	/**
	 * Perform a series of actions for the provided input stream.
	 * 
	 * @param <U> the input type for the repeat actions
	 * @param stream the input stream.
	 * @return a new repeat instance.
	 */
	public static <U> RepeatedPath forEach(Stream<U> stream)
	{
		return new RepeatedPath(stream);
	}
	
	/**
	 * Delay between iterations.
	 * @param duration the duratuon to wait.
	 * @return the repeat instance.
	 */
	public RepeatedPath withDelay(Duration duration)
	{
		this.delay = duration.toMillis();
		return this;
	}

	/**
	 * Navigate to another page.
	 * @param navigate the navigation
	 * @return this instance.
	 */
	@Override
	public RepeatedPath<U> navigateTo(Navigate navigate)
	{
		this.journeySteps.add(new DelayedStep(u -> new NavigateStep(navigate)));
		return this;
	}

	/**
	 * Fills a form.
	 * @param formFill The form to fill.
	 * @return this instance. 
	 */
	@Override
	public <T> RepeatedPath<U> fillForm(FormFill<T> formFill)
	{
		this.journeySteps.add(new DelayedStep(u -> new FillFormStep<T>(formFill)));
		return this;
	}
	
	/**
	 * Fill the form using the repeat input.
	 * @return this instance. 
	 */
	public RepeatedPath<U> fillFormFromInput()
	{
		this.journeySteps.add(new DelayedStep(u -> new FillFormStep<U>(FormFill.fillForm(u))));
		return this;
	}
	
	/**
	 * Scrape a page.
	 * @param scrapePage The oage to scrape.
	 * @return this instance.
	 */
	@Override
	public RepeatedPath<U> scrape(ScrapePage scrapePage)
	{
		this.journeySteps.add(new DelayedStep(u -> new ScrapeStep(scrapePage)));
		return this;
	}
	
	/**
	 * Clicks a button.
	 * @param dismissBanner the banner to dismiss.
	 * @return this instance.
	 */
	@Override
	public RepeatedPath<U> click(ButtonClick dismissBanner)
	{
		this.journeySteps.add(new DelayedStep<>(u -> new ButtonClickStep(dismissBanner)));
		return this;
	}
	
	/**
	 * Continues the journey.
	 * @param context the context of the journey.
	 */
	@Override
	public void followPath(IJourneyContext context)
	{
		this.stream.forEach(u -> doJourney(u, context));
	}

	private void doJourney(U u, IJourneyContext context)
	{
		this.journeySteps.forEach(s -> s.executeStep(u, context));
		try
		{
			Thread.sleep(this.delay);
		}
		catch(InterruptedException ex)
		{
			//Do nothing
		}
	}
	
	/**
	 * Wait for a set duration before continuing the journey.
	 * @param waitFor The wait duration
	 * @return the journey instance.
	 */
	@Override
	public RepeatedPath waitFor(WaitFor waitFor)
	{
		this.journeySteps.add(new DelayedStep<>(u -> new WaitForStep(waitFor)));
		return this;
	}
}

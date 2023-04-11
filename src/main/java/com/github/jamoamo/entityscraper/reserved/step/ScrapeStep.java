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
package com.github.jamoamo.entityscraper.reserved.step;

import com.github.jamoamo.entityscraper.reserved.entity.EntityCreator;
import com.github.jamoamo.entityscraper.api.IJourneyContext;
import com.github.jamoamo.entityscraper.api.ScrapePage;
import com.github.jamoamo.entityscraper.reserved.log.LogMessage;
import com.github.jamoamo.entityscraper.reserved.log.LogSeverity;

/**
 *
 * @author James Amoore
 * @param <E> The class of the Entity to scrape
 */
public final class ScrapeStep<E>
	 extends AJourneyStep
{
	private final ScrapePage<E> scrapePage;

	/**
	 * Creates a new instance.
	 * @param scrapePage The action.
	 */
	public ScrapeStep(ScrapePage<E> scrapePage)
	{
		this.scrapePage = scrapePage;
	}

	/**
	 * Executes the step.
	 * @param context The journey context
	 */
	@Override
	public void executeStep(IJourneyContext context)
	{
		context.log(
				  new LogMessage(
							 LogSeverity.INFO, 
							 "Scraping entity: " + this.scrapePage.getClass().getCanonicalName()));
		
		EntityCreator<E> creator = new EntityCreator(scrapePage.getEntityDefn());
		E instance = creator.createNewEntity(context.getBrowser());
		
		if(this.scrapePage.getScrapeConsumer() != null)
		{
			context.log(
					  new LogMessage(
								 LogSeverity.INFO, 
								 "Consume result with " + this.scrapePage.getClass().getCanonicalName()));
			this.scrapePage.getScrapeConsumer().accept(instance);
		}
	}

}

/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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

import com.github.jamoamo.entityscraper.api.xpath.IPathEvaluator;
import com.github.jamoamo.entityscraper.api.html.IParser;

/**
 * Base EntityScraper builder. Allows for the Html Parser and XPath Evaluator to be specified.
 * Instances of this class are created using the methods on {@link EntityScraperBuilder}
 *
 * @author James Amoore
 */
public final class EntityScraperBuilderBase
{
	private final EntityScrapeContext context;

	EntityScraperBuilderBase(EntityScrapeContext context)
	{
		this.context = context;
	}

	/**
	 * Use the provided {@code IParser} in the built {@code EntityScraper}.
	 *
	 * @param parser The parser to be used by the EntityScraper.
	 *
	 * @return the current instance of {@code EntityScraperBuilderBase}
	 */
	public EntityScraperBuilderBase usingParser(IParser parser)
	{
		if(parser == null)
		{
			throw new IllegalArgumentException("Parser may not be null!");
		}
		
		this.context.setParser(parser);
		return this;
	}

	/**
	 * Use the provided {@link IPathEvaluator} in the built {@link EntityScraper}.
	 *
	 * @param evaluator The path evaluator to be used by the EntityScraper.
	 *
	 * @return the current instance of {@link EntityScraperBuilderBase}
	 */
	public EntityScraperBuilderBase usingPathEvaluator(IPathEvaluator evaluator)
	{
		if(evaluator == null)
		{
			throw new IllegalArgumentException("Evaluator may not be null!");
		}
		
		this.context.setEvaluator(evaluator);
		return this;
	}

	/**
	 * Builds a new instance of {@link EntityScraper}. Repeated calls to this method will return the same instance.
	 *
	 * @return the built {@link EntityScraper}
	 */
	public EntityScraper build()
	{
		EntityScraper scraper = new EntityScraper(this.context);
		return scraper;
	}

}

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
package com.github.jamoamo.entityscraper;

import com.github.jamoamo.entityscraper.xpath.APathEvaluator;
import com.github.jamoamo.entityscraper.html.IParser;

/**
 *
 * @author James Amoore
 */
public final class EntityScraperBuilderBase
{
	private final EntityScraper entityScraper;
	
	EntityScraperBuilderBase(Class<?> entityClass)
	{
		this.entityScraper = new EntityScraper(entityClass);
	}
	
	public EntityScraperBuilderBase usingScraper(IParser scraper)
	{
		this.entityScraper.setScraper(scraper);
		return this;
	}
	
	public EntityScraperBuilderBase usingEvaluator(APathEvaluator evaluator)
	{
		this.entityScraper.setPathEvaluator(evaluator);
		return this;
	}
	
	public EntityScraper build()
	{
		return this.entityScraper;
	}
}

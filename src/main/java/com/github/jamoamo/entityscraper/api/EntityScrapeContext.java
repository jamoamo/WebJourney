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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.api.html.IParser;
import com.github.jamoamo.entityscraper.api.xpath.IPathEvaluator;
import com.github.jamoamo.entityscraper.reserved.html.jsoup.JSoupParser;
import com.github.jamoamo.entityscraper.reserved.xpath.jaxen.JaxenPathEvaluator;

/**
 *
 * @author James Amoore
 */
class EntityScrapeContext
{
	private IPathEvaluator evaluator = new JaxenPathEvaluator();
	private IParser parser = new JSoupParser();
	private final Class entityClass;

	EntityScrapeContext(Class entityClass)
	{
		this.entityClass = entityClass;
	}

	public IPathEvaluator getEvaluator()
	{
		return evaluator;
	}

	public void setEvaluator(IPathEvaluator evaluator)
	{
		this.evaluator = evaluator;
	}

	public Class getEntityClass()
	{
		return this.entityClass;
	}

	void setParser(IParser parser)
	{
		this.parser = parser;
	}
	
	public IParser getParser()
	{
		return this.parser;
	}
}

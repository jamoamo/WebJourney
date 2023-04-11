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
package com.github.jamoamo.entityscraper.api.web;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author James Amoore
 */
public final class WebExtractor implements IWebExtractor
{
	private final IBrowser browser;
	
	/**
	 * Creates a new instance.
	 * @param browser a browser instance.
	 */
	public WebExtractor(IBrowser browser)
	{
		this.browser = browser;
	}

	@Override
	public <T> T extractEntity(String xPath, Function<AElement, T> mappingFunction)
	{
		AElement webElement = this.browser.getElement(xPath);
		return mappingFunction.apply(webElement);
	}

	@Override
	public <T> List<T> extractEntities(String xPath, Function<AElement, T> mappingFunction)
	{
		List<? extends AElement> elements = this.browser.getElements(xPath);
		Stream stream = elements.stream();
		Stream mappedStream = stream.map(mappingFunction);
		return mappedStream.toList();
	}

	@Override
	public String extractValue(String xPath)
	{
		return this.browser.getElement(xPath).getElementText();
	}
	
	@Override
	public <T> T extractValue(String xPath, Function<String, T> mappingFunction)
	{
		return mappingFunction.apply(this.browser.getElement(xPath).getElementText());
	}

	@Override
	public List<String> extractValues(String xPath)
	{
		return this.browser.getElements(xPath).stream().map(e -> e.getElementText()).toList();
	}

	@Override
	public <T> List<T> extractValues(String xPath, Function<String, T> mappingFunction)
	{
		return this.browser.getElementTexts(xPath).stream().map(mappingFunction).toList();
	}
}

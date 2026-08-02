/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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
package io.github.jamoamo.webjourney.reserved.entity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts the values of a node-set of text nodes identified by an xpath.
 *
 * @author James Amoore
 */
class TextNodeCollectionExtractor implements IExtractor<List<String>>
{
	private final String xPath;
	private final ICondition condition;
	private final boolean trim;

	TextNodeCollectionExtractor(
		String xPath,
		ICondition condition)
	{
		this(xPath, condition, true);
	}

	TextNodeCollectionExtractor(
		String xPath,
		ICondition condition,
		boolean trim)
	{
		this.xPath = xPath;
		this.condition = condition;
		this.trim = trim;
	}

	boolean isTrim()
	{
		return this.trim;
	}

	@Override
	public List<String> extractRawValue(IValueReader reader, EntityCreationContext entityCreationContext)
		throws XExtractionException
	{
		try
		{
			List<String> values = reader.getTextNodeValues(this.xPath);
			if(!this.trim)
			{
				return values;
			}
			return values.stream()
				.map(value -> value == null ? null : value.trim())
				.collect(Collectors.toList());
		}
		catch (XValueReaderException ex)
		{
			throw new XExtractionException(
				ex);
		}
	}

	@Override
	public ICondition getCondition()
	{
		return this.condition;
	}

	@Override
	public String describe()
	{
		return "Text Node Collection: " + this.xPath + " on condition: " + this.condition.describe();
	}

}

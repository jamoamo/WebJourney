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

/**
 * Extracts the value of a single text node identified by an xpath.
 *
 * @author James Amoore
 */
class TextNodeExtractor implements IExtractor<String>
{
	private final String xPath;
	private final ICondition condition;
	private final boolean optional;
	private final boolean trim;

	TextNodeExtractor(
		String xPath,
		ICondition condition,
		boolean optional)
	{
		this(xPath, condition, optional, true);
	}

	TextNodeExtractor(
		String xPath,
		ICondition condition,
		boolean optional,
		boolean trim)
	{
		this.xPath = xPath;
		this.condition = condition;
		this.optional = optional;
		this.trim = trim;
	}

	boolean isOptional()
	{
		return this.optional;
	}

	boolean isTrim()
	{
		return this.trim;
	}

	@Override
	public String extractRawValue(IValueReader reader, EntityCreationContext entityCreationContext)
		throws XExtractionException
	{
		try
		{
			String value = reader.getTextNodeValue(this.xPath, this.optional);
			return this.trim && value != null ? value.trim() : value;
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
		return "Text Node: " + this.xPath + " on condition: " + this.condition.describe();
	}

}

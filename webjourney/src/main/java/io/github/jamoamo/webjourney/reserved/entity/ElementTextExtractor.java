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
package io.github.jamoamo.webjourney.reserved.entity;

/**
 *
 * @author James Amoore
 */
class ElementTextExtractor implements IExtractor<String>
{
	private final String xPath;
	private final ICondition condition;
	private final boolean optional;
	
	ElementTextExtractor(String xPath)
	{
		this(xPath, new AlwaysCondition(), false);
	}
	
	ElementTextExtractor(String xPath, ICondition condition, boolean optional)
	{
		this.xPath = xPath;
		this.condition = condition;
		this.optional = optional;
	}
	
	boolean isOptional()
	{
		 return this.optional;
	}

	@Override
	public String extractRawValue(IValueReader reader) throws XExtractionException
	{
		try
		{
			return reader.getElementText(this.xPath, this.optional);
		}
		catch(XValueReaderException ex)
		{
			throw new XExtractionException(ex);
		}
	}
	
	@Override
	public ICondition getCondition()
	{
		return this.condition;
	}
}

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

import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;

/**
 *
 * @author James Amoore
 */
@SuppressWarnings("AbstractClassName")
class AttributeExtractor implements IExtractor<String>
{
	private final String elementXPath;
	private final String attribute;
	private ICondition condition;
	private final boolean optional;
	
	AttributeExtractor(String elementXPath, String attribute)
	{
		this(elementXPath, attribute, new AlwaysCondition(), false);
	}
	
	AttributeExtractor(String elementXPath, String attribute, ICondition condition, boolean optional)
	{
		this.elementXPath = elementXPath;
		this.attribute = attribute;
		this.condition = condition;
		this.optional = optional;
	}

	@Override
	public String extractRawValue(IValueReader browser) throws XExtractionException
	{
		try
		{
			return browser.getElement(this.elementXPath, this.optional).getAttribute(this.attribute);
		}
		catch(XElementDoesntExistException | XValueReaderException ex)
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

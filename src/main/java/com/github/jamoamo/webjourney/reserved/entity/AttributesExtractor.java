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
package com.github.jamoamo.webjourney.reserved.entity;

import java.util.List;
import org.openqa.selenium.NoSuchElementException;

/**
 *
 * @author James Amoore
 */
class AttributesExtractor implements IExtractor<List<String>>
{
	private final String xpath;
	private final String attribute;
	private final ICondition condition;
	private final boolean optional;
	
	AttributesExtractor(String xPath, String attribute, ICondition condition, boolean optional)
	{
		this.xpath = xPath;
		this.attribute = attribute;
		this.condition = condition;
		this.optional = optional;
	}
	
	@Override
	public List<String> extractRawValue(IValueReader browser) throws XExtractionException
	{
		try
		{
			return browser.getElements(this.xpath).stream().map(elem -> elem.getAttribute(this.attribute)).toList();
		}
		catch(NoSuchElementException ex)
		{
			if(this.optional)
			{
				return null;
			}
			throw new XExtractionException(ex);
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

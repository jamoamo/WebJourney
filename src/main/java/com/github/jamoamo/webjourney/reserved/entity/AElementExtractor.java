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

import com.github.jamoamo.webjourney.api.web.AElement;
import org.openqa.selenium.NoSuchElementException;

/**
 *
 * @author James Amoore
 */
abstract class AElementExtractor implements IExtractor<AElement>
{
	private final String xPath;
	private ICondition condition;
	private final boolean optional;
	
	AElementExtractor(String elementXPath, boolean optional)
	{
		this(elementXPath, new AlwaysCondition(), optional);
	}
	
	AElementExtractor(String elementXPath, ICondition condition, boolean optional)
	{
		this.xPath = elementXPath;
		this.condition = condition;
		this.optional = optional;
	}

	@Override
	public AElement extractRawValue(IValueReader reader)
	{
		try
		{
			return reader.getElement(this.xPath);
		}
		catch(NoSuchElementException ex)
		{
			if(this.optional)
			{
				return null;
			}
			throw ex;
		}
	}

	@Override
	public ICondition getCondition()
	{
		return this.condition;
	}
}

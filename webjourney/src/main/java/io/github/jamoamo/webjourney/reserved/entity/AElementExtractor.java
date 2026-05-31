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

import io.github.jamoamo.webjourney.api.web.AElement;
import java.time.Duration;
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
	private final long waitSeconds;

	AElementExtractor(
		String elementXPath,
		boolean optional)
	{
		this(elementXPath, new AlwaysCondition(), optional);
	}

	AElementExtractor(
		String elementXPath,
		ICondition condition,
		boolean optional)
	{
		this(elementXPath, condition, optional, -1);
	}

	AElementExtractor(
		String elementXPath,
		ICondition condition,
		boolean optional,
		long waitSeconds)
	{
		this.xPath = elementXPath;
		this.condition = condition;
		this.optional = optional;
		this.waitSeconds = waitSeconds;
	}

	@Override
	public AElement extractRawValue(IValueReader reader, EntityCreationContext entityCreationContext)
		throws XExtractionException
	{
		try
		{
			Duration wait = ElementWaitResolver.resolve(this.waitSeconds, entityCreationContext);
			if(wait.isZero())
			{
				return reader.getElement(this.xPath, this.optional);
			}
			return reader.getElement(this.xPath, this.optional, wait);
		}
		catch (NoSuchElementException ex)
		{
			if (this.optional)
			{
				return null;
			}
			throw new XExtractionException(
				ex);
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
		return "Element: " + this.xPath + " on condition: " + this.condition.describe();
	}

}

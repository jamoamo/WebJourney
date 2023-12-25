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
package com.github.jamoamo.webjourney.reserved.selenium;

import com.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
class ChildElementListItemLocator implements ISeleniumElementLocator
{
	private final SeleniumElement element;
	private final By by;
	private final int index;
	private final boolean optional;
	
	ChildElementListItemLocator(SeleniumElement element, By by, int index, boolean optional)
	{
		this.element = element;
		this.by = by;
		this.index = index;
		this.optional = optional;
	}
	
	@Override
	public WebElement findElement() throws XElementDoesntExistException
	{
		try
		{
			return this.element.getWebElement().findElements(this.by).get(this.index);
		}
		catch(NoSuchElementException | IndexOutOfBoundsException ex)
		{
			if(this.optional)
			{
				return null;
			}
			throw new XElementDoesntExistException();
		}
	}
	
}

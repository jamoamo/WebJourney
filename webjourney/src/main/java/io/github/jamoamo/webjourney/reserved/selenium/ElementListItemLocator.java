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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Locates one item of a list of elements on the page.
 *
 * <p>
 * The item's element is cached once found, and the list is normally resolved once for all its items, so each item is
 * handed its element up front. Re-running the list query on every access made extracting N items cost N list queries
 * per field, and ChromeDriver keeps every element reference those queries return in the page (ARM-465). The query is
 * only run again after {@link #invalidate()}, when the cached element has gone stale.
 * </p>
 *
 * @author James Amoore
 */
class ElementListItemLocator implements ISeleniumElementLocator
{
	private final WebDriver driver;
	private final By by;
	private final int index;
	private WebElement cached;

	ElementListItemLocator(
		WebDriver driver,
		By by,
		int index)
	{
		this(driver, by, index, null);
	}

	ElementListItemLocator(
		WebDriver driver,
		By by,
		int index,
		WebElement resolved)
	{
		this.driver = driver;
		this.by = by;
		this.index = index;
		this.cached = resolved;
	}

	@Override
	public WebElement findElement() throws XElementDoesntExistException
	{
		if (this.cached == null)
		{
			this.cached = locate();
		}
		return this.cached;
	}

	@Override
	public void invalidate()
	{
		this.cached = null;
	}

	private WebElement locate() throws XElementDoesntExistException
	{
		try
		{
			return this.driver.findElements(this.by).get(this.index);
		}
		catch (IndexOutOfBoundsException ex)
		{
			throw new XElementDoesntExistException(
				"Elements Identified By: " + this.by.toString() + " at index " + 
				this.index + " on page " + this.driver.getCurrentUrl() + " don't exist.");
		}
	}

}

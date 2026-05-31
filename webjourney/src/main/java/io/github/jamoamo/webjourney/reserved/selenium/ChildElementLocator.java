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
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

/**
 *
 * @author James Amoore
 */

class ChildElementLocator implements ISeleniumElementLocator
{
	private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

	private final SeleniumElement element;
	private final By by;
	private final boolean optional;
	private final Duration wait;

	ChildElementLocator(
		SeleniumElement element,
		By by,
		boolean optional)
	{
		this(element, by, optional, Duration.ZERO);
	}

	ChildElementLocator(
		SeleniumElement element,
		By by,
		boolean optional,
		Duration wait)
	{
		this.element = element;
		this.by = by;
		this.optional = optional;
		this.wait = wait;
	}

	@Override
	public WebElement findElement() throws XElementDoesntExistException
	{
		WebElement parent = this.element.getWebElement();

		if (parent == null)
		{
			if (this.optional)
			{
				return null;
			}
			throw new XElementDoesntExistException(
				"Parent element missing for child identified by: " + this.by.toString());
		}

		if (this.wait != null && !this.wait.isZero() && !this.wait.isNegative())
		{
			return findChildWaiting(parent);
		}

		try
		{
			return parent.findElement(this.by);
		}
		catch (NoSuchElementException ex)
		{
			return handleMissingElement();
		}
	}

	private WebElement findChildWaiting(WebElement parent) throws XElementDoesntExistException
	{
		try
		{
			return new FluentWait<>(parent)
				.withTimeout(this.wait)
				.pollingEvery(POLL_INTERVAL)
				.ignoring(NoSuchElementException.class)
				.until(p -> p.findElement(this.by));
		}
		catch (TimeoutException ex)
		{
			return handleMissingElement();
		}
	}

	private WebElement handleMissingElement() throws XElementDoesntExistException
	{
		if (this.optional)
		{
			return null;
		}
		throw new XElementDoesntExistException(
			"Element Identified By: " + this.by.toString() + " doesn't exist.");
	}

}

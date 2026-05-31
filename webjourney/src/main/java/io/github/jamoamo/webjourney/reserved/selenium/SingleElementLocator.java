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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author James Amoore
 */
class SingleElementLocator implements ISeleniumElementLocator
{
	private final WebDriver driver;
	private final By by;
	private final boolean optional;
	private final Duration wait;

	SingleElementLocator(
		WebDriver driver,
		By by,
		boolean optional)
	{
		this(driver, by, optional, Duration.ZERO);
	}

	SingleElementLocator(
		WebDriver driver,
		By by,
		boolean optional,
		Duration wait)
	{
		this.driver = driver;
		this.by = by;
		this.optional = optional;
		this.wait = wait;
	}

	@Override
	public WebElement findElement() throws XElementDoesntExistException
	{
		if (this.wait != null && !this.wait.isZero() && !this.wait.isNegative())
		{
			return findElementWaiting();
		}
		try
		{
			return this.driver.findElement(this.by);
		}
		catch (NoSuchElementException ex)
		{
			return handleMissingElement();
		}
	}

	private WebElement findElementWaiting() throws XElementDoesntExistException
	{
		try
		{
			return new WebDriverWait(this.driver, this.wait)
				.until(ExpectedConditions.presenceOfElementLocated(this.by));
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
			"Element Identified By: " + this.by.toString() + " on page " + this.driver.getCurrentUrl() + " doesn't exist.");
	}

}

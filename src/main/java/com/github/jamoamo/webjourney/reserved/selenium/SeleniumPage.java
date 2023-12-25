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

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IWebPage;
import java.util.List;
import org.apache.commons.lang3.stream.IntStreams;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author James Amoore
 */
final class SeleniumPage implements IWebPage
{
	private final RemoteWebDriver webDriver;
	SeleniumPage(RemoteWebDriver webDriver)
	{
		this.webDriver = webDriver;
	}

	@Override
	public AElement getElement(String xPath)
	{
		return getElement(xPath, false);
	}
	
	@Override
	public AElement getElement(String xPath, boolean optional)
	{
		return new SeleniumElement(new SingleElementLocator(this.webDriver, By.xpath(xPath), optional));
	}

	@Override
	public List<? extends AElement> getElements(String xPath)
	{
		return IntStreams.range(this.webDriver.findElements(By.xpath(xPath)).size())
			.mapToObj(i -> new ElementListItemLocator(this.webDriver, By.xpath(xPath), i))
			.map(locator -> new SeleniumElement(locator))
			.toList();
	}

	@Override
	public List<? extends AElement> getElementsByTag(String tag)
	{
		return IntStreams.range(this.webDriver.findElements(By.tagName(tag)).size())
			.mapToObj(i -> new ElementListItemLocator(this.webDriver, By.tagName(tag), i))
			.map(locator -> new SeleniumElement(locator))
			.toList();
	}
	
}

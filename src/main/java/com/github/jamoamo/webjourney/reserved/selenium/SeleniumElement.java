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
import java.util.List;
import org.apache.commons.lang3.stream.IntStreams;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
class SeleniumElement extends AElement
{
	private WebElement webElement;
	
	private ISeleniumElementLocator locator;

	SeleniumElement(WebElement webElement)
	{
		this.webElement = webElement;
	}
	
	SeleniumElement(ISeleniumElementLocator locator)
	{
		this.locator = locator;
	}

	@Override
	public String getElementText()
	{
		return getElement().getText();
	}

	@Override
	public AElement findElement(String path)
	{
		return new SeleniumElement(new ChildElementLocator(this, By.xpath(path)));
	}

	@Override
	public List<? extends AElement> findElements(String path)
	{
		return IntStreams
			.range(getElement().findElements(By.xpath(path)).size())
			.mapToObj(i -> new ChildElementListItemLocator(this, By.xpath(path), i))
			.map(locator -> new SeleniumElement(locator))
			.toList();
	}

	@Override
	public String getAttribute(String attribute)
	{
		return getElement().getAttribute(attribute);
	}

	@Override
	public void click()
	{
		getElement().click();
	}

	@Override
	public void enterText(String text)
	{
		getElement().sendKeys(text);
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType)
	{
		return IntStreams
			.range(getElement().findElements(By.tagName(childElementType)).size())
			.mapToObj(i -> new ChildElementListItemLocator(this, By.tagName(childElementType), i))
			.map(locator -> new SeleniumElement(locator))
			.toList();
	}

	@Override
	public String getTag()
	{
		return getElement().getTagName();
	}
	
	private WebElement getElement()
	{
		if(this.webElement != null)
		{
			return this.webElement;
		}
		else
		{
			return this.locator.findElement();
		}
	}

	WebElement getWebElement()
	{
		return getElement();
	}
}

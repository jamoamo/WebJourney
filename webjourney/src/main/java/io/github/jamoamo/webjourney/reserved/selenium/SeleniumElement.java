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
import io.github.jamoamo.webjourney.api.web.AElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.stream.IntStreams;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
class SeleniumElement extends AElement
{
	private final ISeleniumElementLocator locator;
	private final ScriptExecutor executor;

	SeleniumElement(ISeleniumElementLocator locator)
	{
		this(locator, null);
	}
	
	SeleniumElement(ISeleniumElementLocator locator, ScriptExecutor executor)
	{
		this.locator = locator;
		this.executor = executor;
	}

	@Override
	public String getElementText() throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		if(elem.isEmpty())
		{
			return null;
		}
		
		return elem.get().getText();
	}

	@Override
	public AElement findElement(String path)
	{
		return new SeleniumElement(new ChildElementLocator(this, By.xpath(path), false), this.executor);
	}

	@Override
	public List<? extends AElement> findElements(String path) throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		if(elem.isEmpty())
		{
			return new ArrayList<>();
		}
		
		return IntStreams
			.range(elem.get().findElements(By.xpath(path)).size())
			.mapToObj(i -> new ChildElementListItemLocator(this, By.xpath(path), i, false))
			.map(locator -> new SeleniumElement(locator, this.executor))
			.toList();
	}

	@Override
	public String getAttribute(String attribute) throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		if(elem.isEmpty())
		{
			return null;
		}
		return elem.get().getAttribute(attribute);
	}

	@Override
	public void click() throws XElementDoesntExistException
	{
		try
		{
			getElement().ifPresent(e -> e.click());
		}
		catch(ElementClickInterceptedException ex)
		{
			if(this.executor != null && getElement().isPresent())
			{
				this.executor.executeScript("arguments[0].click();", getElement().get());
			}
			else
			{
				throw ex;
			}
		}
	}

	@Override
	public void enterText(String text) throws XElementDoesntExistException
	{
		getElement().ifPresent(e -> e.sendKeys(text));
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType) throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		if(!elem.isPresent())
		{
			return new ArrayList<>();
		}
		
		return IntStreams
			.range(elem.get().findElements(By.tagName(childElementType)).size())
			.mapToObj(i -> new ChildElementListItemLocator(this, By.tagName(childElementType), i, false))
			.map(locator -> new SeleniumElement(locator, this.executor))
			.toList();
	}

	@Override
	public String getTag() throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		return elem.isPresent() ? elem.get().getTagName() : null;
	}
	
	private Optional<WebElement> getElement() throws XElementDoesntExistException
	{
		return Optional.ofNullable(this.locator.findElement());
	}

	WebElement getWebElement() throws XElementDoesntExistException
	{
		return getElement().orElse(null);
	}

	@Override
	public boolean exists()
	{
		try
		{
			return getElement().isPresent();
		}
		catch(XElementDoesntExistException ex)
		{
			return false;
		}
	}
}

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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.stream.IntStreams;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
class SeleniumElement extends AElement
{
	private static final int DOM_TEXT_NODE_TYPE = 3;

	private static final String TEXT_NODE_XPATH_SCRIPT =
		"var result = document.evaluate(arguments[1], arguments[0], null, "
		+ "XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);"
		+ "var values = [];"
		+ "for (var i = 0; i < result.snapshotLength; i++) {"
		+ "var node = result.snapshotItem(i);"
		+ "if (node.nodeType === " + DOM_TEXT_NODE_TYPE + ") {"
		+ "values.push(node.textContent);"
		+ "}"
		+ "}"
		+ "return values;";

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
	public AElement findElement(String path, boolean optional)
	{
		return new SeleniumElement(new ChildElementLocator(this, By.xpath(path), optional), this.executor);
	}

	@Override
	public AElement findElement(String path, boolean optional, Duration wait)
	{
		return new SeleniumElement(new ChildElementLocator(this, By.xpath(path), optional, wait), this.executor);
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

	@Override
	public List<String> getTextNodeValues(String xPath) throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		if(elem.isEmpty())
		{
			return new ArrayList<>();
		}
		if(this.executor == null)
		{
			throw new IllegalStateException("No script executor available to evaluate xpath text nodes.");
		}

		Object result;
		try
		{
			result = this.executor.executeScript(TEXT_NODE_XPATH_SCRIPT, elem.get(), xPath);
		}
		catch(WebDriverException ex)
		{
			// e.g. a malformed xpath surfaces as a JS SyntaxError, wrapped by Selenium as a WebDriverException.
			throw new XElementDoesntExistException(
				"Unable to evaluate text node xpath [" + xPath + "]: " + ex.getMessage());
		}
		if(!(result instanceof List<?> rawValues))
		{
			return new ArrayList<>();
		}
		return rawValues.stream()
			.map(value -> value == null ? null : value.toString())
			.toList();
	}
}

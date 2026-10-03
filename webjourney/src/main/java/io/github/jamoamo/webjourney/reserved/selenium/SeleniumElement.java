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
import java.util.function.Function;
import java.util.stream.IntStream;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * An element on a Selenium-driven page.
 *
 * <p>
 * Elements that are items of a list cache the {@link WebElement} they were resolved to (ARM-465). If a cached element
 * has gone stale, the operation discards it, locates the element again and is retried once.
 * </p>
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
		return withElement(WebElement::getText, null);
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
		return childListItems(By.xpath(path));
	}

	@Override
	public String getAttribute(String attribute) throws XElementDoesntExistException
	{
		return withElement(e -> e.getAttribute(attribute), null);
	}

	@Override
	public void click() throws XElementDoesntExistException
	{
		try
		{
			withElement(e ->
			{
				e.click();
				return null;
			}, null);
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
		withElement(e ->
		{
			e.sendKeys(text);
			return null;
		}, null);
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType) throws XElementDoesntExistException
	{
		return childListItems(By.tagName(childElementType));
	}

	@Override
	public String getTag() throws XElementDoesntExistException
	{
		return withElement(WebElement::getTagName, null);
	}

	private Optional<WebElement> getElement() throws XElementDoesntExistException
	{
		return Optional.ofNullable(this.locator.findElement());
	}

	WebElement getWebElement() throws XElementDoesntExistException
	{
		return getElement().orElse(null);
	}

	/**
	 * Discards any element this element's locator has cached, so it is located again on next use.
	 */
	void invalidate()
	{
		this.locator.invalidate();
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
		Object result;
		try
		{
			result = withElement(e ->
			{
				if(this.executor == null)
				{
					throw new IllegalStateException("No script executor available to evaluate xpath text nodes.");
				}
				return Optional.ofNullable(this.executor.executeScript(TEXT_NODE_XPATH_SCRIPT, e, xPath));
			}, Optional.empty())
				.orElse(null);
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

	/**
	 * Resolves the child list once and hands each item its element, so reading the items does not re-run the list
	 * query for every item (ARM-465).
	 */
	private List<? extends AElement> childListItems(By by) throws XElementDoesntExistException
	{
		List<WebElement> children = withElement(e -> e.findElements(by), List.of());
		return IntStream.range(0, children.size())
			.mapToObj(i -> new ChildElementListItemLocator(this, by, i, false, children.get(i)))
			.map(childLocator -> new SeleniumElement(childLocator, this.executor))
			.toList();
	}

	/**
	 * Applies an action to this element, or returns {@code whenAbsent} if there is no element. If the element has
	 * gone stale, it is discarded, located again and the action is retried once.
	 */
	private <T> T withElement(Function<WebElement, T> action, T whenAbsent) throws XElementDoesntExistException
	{
		try
		{
			return applyToElement(action, whenAbsent);
		}
		catch(StaleElementReferenceException ex)
		{
			this.locator.invalidate();
			return applyToElement(action, whenAbsent);
		}
	}

	private <T> T applyToElement(Function<WebElement, T> action, T whenAbsent) throws XElementDoesntExistException
	{
		Optional<WebElement> elem = getElement();
		return elem.isEmpty() ? whenAbsent : action.apply(elem.get());
	}
}

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
import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.ICookie;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A browser that uses Selenium to drive the browser interactions.
 *
 * @author James Amoore
 */
class SeleniumDrivenBrowser implements IBrowser
{
	private static class Element extends AElement
	{
		private final WebElement webElement;

		Element(WebElement webElement)
		{
			this.webElement = webElement;
		}

		@Override
		public String getElementText()
		{
			return this.webElement.getText();
		}

		@Override
		public AElement findElement(String path)
		{
			return new Element(this.webElement.findElement(By.xpath(path)));
		}

		@Override
		public List<? extends AElement> findElements(String path)
		{
			return this.webElement.findElements(By.xpath(path)).stream().map(e -> new Element(e)).toList();
		}

		@Override
		public String getAttribute(String attribute)
		{
			return this.webElement.getAttribute(attribute);
		}
	}

	private final static int DEFAULT_TIMEOUT_MINUTES = 1;
	private final static int DEFAULT_POLLING_SECONDS = 10;

	private final Logger logger = LoggerFactory.getLogger(IBrowser.class);

	private final WebDriver driver;

	SeleniumDrivenBrowser(WebDriver driver)
	{
		this.driver = driver;
		this.driver.manage().timeouts().implicitlyWait(Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES));
	}

	@Override
	public void navigateToUrl(URL url)
	{
		this.logger.info("Navigate to " + url.toExternalForm());

		this.driver.navigate().to(url);
		waitForPageToLoad(url.toExternalForm());
	}

	@Override
	public void fillElement(String xPathExpression, String value)
	{
		this.logger.info("Fill Element with path " + xPathExpression);

		WebElement element = this.driver.findElement(By.xpath(xPathExpression));
		element.clear();
		element.sendKeys(value);
	}

	@Override
	public <T> List<T> getElementEntities(String xPath, Function<WebElement, T> function)
	{
		this.logger.info("Getting Element Entities with path: " + xPath);
		List<WebElement> webElements = this.driver.findElements(By.xpath(xPath));
		return webElements.stream().map(function).toList();
	}

	@Override
	public String getElementText(String xPathExpression)
	{
		this.logger.info("Get text from Element with path " + xPathExpression);
		return this.driver.findElement(By.xpath(xPathExpression)).getText();
	}

	@Override
	public List<String> getElementTexts(String xPathExpression)
	{
		this.logger.info("Get text from Elements with path " + xPathExpression);

		List<WebElement> findElements = this.driver.findElements(By.xpath(xPathExpression));
		List<String> value = new ArrayList<>();
		for(WebElement element : findElements)
		{
			String text = element.getText();
			value.add(text);
		}
		return value;
	}

	@Override
	public void clickElement(String xPathExpression)
	{
		this.clickElement(xPathExpression, false);
	}

	@Override
	public void clickElement(String xPathExpression, boolean ignoreIfNotPresent)
	{
		this.logger.info("Click element " + xPathExpression);
		try
		{
			WebElement element = this.driver.findElement(By.xpath(xPathExpression));
			if(ignoreIfNotPresent && (!element.isDisplayed() || !element.isEnabled()))
			{
				return;
			}
			waitForElementToBeClickable(element);
			element.click();
		}
		catch(NoSuchElementException ex)
		{
			if(!ignoreIfNotPresent)
			{
				throw ex;
			}
		}
	}

	@Override
	public void exit()
	{
		this.logger.info("Exit browser");
		this.driver.close();
		this.driver.quit();
	}

	@Override
	public void waitForAllElements(String... xPathExpressions)
	{
		this.logger.info("Wait for elements: " + Arrays.toString(xPathExpressions));

		List<WebElement> elements =
				  Arrays.stream(xPathExpressions).map(xPath -> this.driver.findElement(By.xpath(xPath))).toList();

		FluentWait wait = new FluentWait(this.driver);
		wait.withTimeout(Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES))
				  .pollingEvery(Duration.ofSeconds(DEFAULT_POLLING_SECONDS))
				  .until(ExpectedConditions.visibilityOfAllElements(elements));
	}

	private void waitForElementToBeClickable(WebElement element)
	{
		FluentWait wait = new FluentWait(this.driver);
		wait.withTimeout(Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES))
				  .pollingEvery(Duration.ofSeconds(DEFAULT_POLLING_SECONDS))
				  .until(ExpectedConditions.elementToBeClickable(element));
	}

	private void waitForPageToLoad(String url)
	{
		FluentWait wait = new FluentWait(this.driver);
		wait.withTimeout(Duration.ofMinutes(DEFAULT_TIMEOUT_MINUTES))
				  .pollingEvery(Duration.ofSeconds(DEFAULT_POLLING_SECONDS))
				  .until(ExpectedConditions.urlContains(url));
	}

	@Override
	public AElement getElement(String xPath)
	{
		WebElement element = this.driver.findElement(By.xpath(xPath));
		return new Element(element);
	}

	@Override
	public List<? extends AElement> getElements(String xPath)
	{
		List<WebElement> elements = this.driver.findElements(By.xpath(xPath));
		return elements.stream().map(we -> new Element(we)).toList();
	}

	@Override
	public List<? extends AElement> getChildElementsByTag(String xPath, String tag)
	{
		return this.driver.findElement(By.xpath(xPath)).findElements(By.tagName(tag)).stream().
				  map(we -> new Element(we)).toList();
	}

	@Override
	public void navigateBack()
	{
		this.driver.navigate().back();
	}

	@Override
	public void navigateForward()
	{
		this.driver.navigate().forward();
	}

	@Override
	public void refreshPage()
	{
		this.driver.navigate().refresh();
	}

	@Override
	public Collection<ICookie> getCookies()
	{
		return this.driver.manage().getCookies()
				  .stream()
				  .map(c -> new SeleniumCookieAdapter(c)).collect(Collectors.toSet());
	}

	@Override
	public String getCurrentUrl()
	{
		return this.driver.getCurrentUrl();
	}

}

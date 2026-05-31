/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Test class for SingleElementLocator wait behaviour.
 */
public class SingleElementLocatorTest
{
	private static final Duration SHORT_WAIT = Duration.ofMillis(200);

	@Test
	public void testFindElement_NoWait_ReturnsElement() throws XElementDoesntExistException
	{
		WebDriver driver = Mockito.mock(WebDriver.class);
		WebElement element = Mockito.mock(WebElement.class);
		By by = By.xpath("//div");
		when(driver.findElement(by)).thenReturn(element);

		SingleElementLocator locator = new SingleElementLocator(driver, by, false);

		assertSame(element, locator.findElement());
	}

	@Test
	public void testFindElement_WithWait_ReturnsPresentElement() throws XElementDoesntExistException
	{
		WebDriver driver = Mockito.mock(WebDriver.class);
		WebElement element = Mockito.mock(WebElement.class);
		By by = By.xpath("//div");
		when(driver.findElement(by)).thenReturn(element);

		SingleElementLocator locator = new SingleElementLocator(driver, by, false, SHORT_WAIT);

		assertSame(element, locator.findElement());
	}

	@Test
	public void testFindElement_WithWait_OptionalMissing_ReturnsNull() throws XElementDoesntExistException
	{
		WebDriver driver = Mockito.mock(WebDriver.class);
		By by = By.xpath("//div");
		when(driver.findElement(by)).thenThrow(new NoSuchElementException("missing"));

		SingleElementLocator locator = new SingleElementLocator(driver, by, true, SHORT_WAIT);

		assertNull(locator.findElement());
		Mockito.verify(driver, atLeastOnce()).findElement(by);
	}

	@Test
	public void testFindElement_WithWait_RequiredMissing_Throws()
	{
		WebDriver driver = Mockito.mock(WebDriver.class);
		By by = By.xpath("//div");
		when(driver.findElement(by)).thenThrow(new NoSuchElementException("missing"));
		when(driver.getCurrentUrl()).thenReturn("http://example.com");

		SingleElementLocator locator = new SingleElementLocator(driver, by, false, SHORT_WAIT);

		assertThrows(XElementDoesntExistException.class, locator::findElement);
	}
}

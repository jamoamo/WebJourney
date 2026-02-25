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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ChildElementLocator.
 */
public class ChildElementLocatorTest
{

	@Test
	public void testFindElement_ParentNull_OptionalTrue() throws XElementDoesntExistException
	{
		// Arrange
		SeleniumElement mockParent = Mockito.mock(SeleniumElement.class);
		when(mockParent.getWebElement()).thenReturn(null);

		ChildElementLocator locator = new ChildElementLocator(
			mockParent,
			By.xpath("//div"),
			true);

		// Act
		WebElement result = locator.findElement();

		// Assert
		assertNull(result);
	}

	@Test
	public void testFindElement_ParentNull_OptionalFalse() throws XElementDoesntExistException
	{
		// Arrange
		SeleniumElement mockParent = Mockito.mock(SeleniumElement.class);
		when(mockParent.getWebElement()).thenReturn(null);

		ChildElementLocator locator = new ChildElementLocator(
			mockParent,
			By.xpath("//div"),
			false);

		// Act & Assert
		try
		{
			locator.findElement();
			fail("Expected XElementDoesntExistException was not thrown");
		}
		catch (XElementDoesntExistException e)
		{
			assertTrue(e.getMessage().contains("Parent element missing"));
		}
	}

	@Test
	public void testFindElement_ParentExists_ReturnsChild() throws XElementDoesntExistException
	{
		// Arrange
		SeleniumElement mockParent = Mockito.mock(SeleniumElement.class);
		WebElement mockParentElement = Mockito.mock(WebElement.class);
		WebElement mockChildElement = Mockito.mock(WebElement.class);
		By by = By.xpath("//div");

		when(mockParent.getWebElement()).thenReturn(mockParentElement);
		when(mockParentElement.findElement(by)).thenReturn(mockChildElement);

		ChildElementLocator locator = new ChildElementLocator(
			mockParent,
			by,
			false);

		// Act
		WebElement result = locator.findElement();

		// Assert
		assertEquals(mockChildElement, result);
	}
}

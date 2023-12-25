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
import com.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.iterators.SingletonIterator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
public class SeleniumElementTest
{
	private static ISeleniumElementLocator locator = Mockito.mock(ISeleniumElementLocator.class);
	private static WebElement webElementMock = Mockito.mock(WebElement.class);
	
	/**
	 * Test of getElementText method, of class SeleniumElement.
	 */
	@Test
	public void testGetElementText() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		Mockito.when(webElementMock.getText()).thenReturn("Text");
		
		SeleniumElement element = new SeleniumElement(locator);
		String result = element.getElementText();
		assertEquals("Text", result);
	}

	/**
	 * Test of findElement method, of class SeleniumElement.
	 */
	@Test
	public void testFindElement() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		WebElement elem = Mockito.mock(WebElement.class);
		Mockito.when(elem.getText()).thenReturn("Inner Element");
		
		Mockito.when(webElementMock.findElement(any())).thenReturn(elem);
		
		SeleniumElement element = new SeleniumElement(locator);
		AElement result = element.findElement("//div");
		assertEquals("Inner Element", result.getElementText());
	}

	/**
	 * Test of findElements method, of class SeleniumElement.
	 */
	@Test
	public void testFindElements() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		WebElement elem1 = Mockito.mock(WebElement.class);
		Mockito.when(elem1.getText()).thenReturn("Inner Element");
		
		Mockito.when(webElementMock.findElements(any())).thenReturn(Collections.singletonList(elem1));
		
		SeleniumElement element = new SeleniumElement(locator);
		List<? extends AElement> result = element.findElements("//div");
		assertEquals(1, result.size());
		assertEquals("Inner Element", result.get(0).getElementText());
	}

	/**
	 * Test of getAttribute method, of class SeleniumElement.
	 */
	@Test
	public void testGetAttribute() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		Mockito.when(webElementMock.getAttribute("Attr")).thenReturn("Value");
		
		SeleniumElement element = new SeleniumElement(locator);
		String result = element.getAttribute("Attr");
		assertEquals("Value", result);
	}

	/**
	 * Test of click method, of class SeleniumElement.
	 */
	@Test
	public void testClick() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		SeleniumElement element = new SeleniumElement(locator);
		
		element.click();
		
		verify(webElementMock).click();
	}

	/**
	 * Test of enterText method, of class SeleniumElement.
	 */
	@Test
	public void testEnterText() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		SeleniumElement element = new SeleniumElement(locator);
		
		element.enterText("Text Value");
		
		ArgumentCaptor<CharSequence> keysArg = ArgumentCaptor.forClass(CharSequence.class);
		verify(webElementMock).sendKeys(keysArg.capture());
		
		assertEquals("Text Value", keysArg.getValue());
	}

	/**
	 * Test of getChildrenByTag method, of class SeleniumElement.
	 */
	@Test
	public void testGetChildrenByTag() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		WebElement elem1 = Mockito.mock(WebElement.class);
		Mockito.when(elem1.getText()).thenReturn("Inner Element");
		
		Mockito.when(webElementMock.findElements(By.tagName("div"))).thenReturn(Collections.singletonList(elem1));
		
		SeleniumElement element = new SeleniumElement(locator);
		List<? extends AElement> result = element.getChildrenByTag("div");
		assertEquals(1, result.size());
		assertEquals("Inner Element", result.get(0).getElementText());
	}

	/**
	 * Test of getTag method, of class SeleniumElement.
	 */
	@Test
	public void testGetTag() throws XElementDoesntExistException
	{
		Mockito.when(locator.findElement()).thenReturn(webElementMock);
		Mockito.when(webElementMock.getTagName()).thenReturn("td");
		SeleniumElement element = new SeleniumElement(locator);
		
		String tag = element.getTag();
		
		assertEquals("td", tag);
	}
	
}

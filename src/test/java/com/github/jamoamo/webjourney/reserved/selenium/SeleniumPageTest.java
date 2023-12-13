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
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author James Amoore
 */
public class SeleniumPageTest
{
	private WebDriver driverMock = Mockito.mock(WebDriver.class);

	/**
	 * Test of getElement method, of class SeleniumPage.
	 */
	@Test
	public void testGetElement()
	{
		WebElement elem = Mockito.mock(WebElement.class);
		Mockito.when(elem.getText()).thenReturn("Elem Text");
		
		Mockito.when(driverMock.findElement(By.xpath("//div"))).thenReturn(elem);
		SeleniumPage page = new SeleniumPage(driverMock);
		
		AElement element = page.getElement("//div");
		assertEquals("Elem Text", element.getElementText());
	}

	/**
	 * Test of getElements method, of class SeleniumPage.
	 */
	@Test
	public void testGetElements()
	{
		WebElement elem = Mockito.mock(WebElement.class);
		Mockito.when(elem.getText()).thenReturn("Elem Text");
		
		Mockito.when(driverMock.findElements(By.xpath("//div"))).thenReturn(Collections.singletonList(elem));
		SeleniumPage page = new SeleniumPage(driverMock);
		
		List<? extends AElement> elements = page.getElements("//div");
		assertEquals(1, elements.size());
		assertEquals("Elem Text", elements.get(0).getElementText());
	}

	/**
	 * Test of getElementsByTag method, of class SeleniumPage.
	 */
	@Test
	public void testGetElementsByTag()
	{
		WebElement elem = Mockito.mock(WebElement.class);
		Mockito.when(elem.getText()).thenReturn("Elem Text");
		
		Mockito.when(driverMock.findElements(By.tagName("div"))).thenReturn(Collections.singletonList(elem));
		SeleniumPage page = new SeleniumPage(driverMock);
		
		List<? extends AElement> elements = page.getElementsByTag("div");
		assertEquals(1, elements.size());
		assertEquals("Elem Text", elements.get(0).getElementText());
	}
	
}

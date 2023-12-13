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
package com.github.jamoamo.webjourney.reserved.entity;

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.IBrowserWindow;
import com.github.jamoamo.webjourney.api.web.IWebPage;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author James Amoore
 */
public class BrowserValueReaderTest
{
	/**
	 * Test of getCurrentUrl method, of class BrowserValueReader.
	 */
	@Test
	public void testGetCurrentUrl()
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentUrl()).thenReturn("https://current.url");
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		mockBrowser.getActiveWindow();
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		String currentUrl = reader.getCurrentUrl();
		assertEquals("https://current.url", currentUrl);
	}

	/**
	 * Test of getElementText method, of class BrowserValueReader.
	 */
	@Test
	public void testGetElementText()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getElementText()).thenReturn("Element Value");
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("//div")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		String text = reader.getElementText("//div");
		assertEquals("Element Value", text);
	}

	/**
	 * Test of getElement method, of class BrowserValueReader.
	 */
	@Test
	public void testGetElement()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getElementText()).thenReturn("Element Value");
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("//div")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		AElement elementResult = reader.getElement("//div");
		assertSame(element, elementResult);
	}

	/**
	 * Test of getAttribute method, of class BrowserValueReader.
	 */
	@Test
	public void testGetAttribute()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getElementText()).thenReturn("Element Value");
		Mockito.when(element.getAttribute("attr")).thenReturn("Attribute Value");
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("//div")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		String attribute = reader.getAttribute("//div", "attr");
		assertSame("Attribute Value", attribute);
	}

	/**
	 * Test of navigateTo method, of class BrowserValueReader.
	 */
	@Test
	public void testNavigateTo() throws Exception
	{
		IWebPage page = Mockito.mock(IWebPage.class);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		URL url = new URL("https://navigate.url");
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		reader.navigateTo(url);
		
		verify(window).navigateToUrl(url);
	}

	/**
	 * Test of navigateBack method, of class BrowserValueReader.
	 */
	@Test
	public void testNavigateBack() throws Exception
	{
		IWebPage page = Mockito.mock(IWebPage.class);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		reader.navigateBack();
		
		verify(window).navigateBack();
	}

	/**
	 * Test of getElements method, of class BrowserValueReader.
	 */
	@Test
	public void testGetElements()
	{
		AElement element = Mockito.mock(AElement.class);
		
		Answer<List<AElement>> answer = new Answer<List<AElement>>()
		{
			@Override
			public List<AElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return Collections.singletonList(element);
			}
		};
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElements("//div")).thenAnswer(answer);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		List<? extends AElement> elements = reader.getElements("//div");
		
		assertEquals(1, elements.size());
	}

	/**
	 * Test of getElementTexts method, of class BrowserValueReader.
	 */
	@Test
	public void testGetElementTexts()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getElementText()).thenReturn("Text");
		
		Answer<List<AElement>> answer = new Answer<List<AElement>>()
		{
			@Override
			public List<AElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return Collections.singletonList(element);
			}
		};
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElements("//div")).thenAnswer(answer);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		List<String> elementTexts = reader.getElementTexts("//div");
		
		assertEquals(1, elementTexts.size());
		assertEquals("Text", elementTexts.get(0));
	}

	/**
	 * Test of openNewWindow method, of class BrowserValueReader.
	 */
	@Test
	public void testOpenNewWindow()
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		reader.openNewWindow();
		
		verify(mockBrowser).openNewWindow();
	}

	/**
	 * Test of closeWindow method, of class BrowserValueReader.
	 */
	@Test
	public void testCloseWindow()
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(window);
		
		BrowserValueReader reader = new BrowserValueReader(mockBrowser);
		reader.closeWindow();
		
		verify(window).close();
	}
}

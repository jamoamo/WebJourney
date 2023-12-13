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
package com.github.jamoamo.webjourney.api.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class WebUserTest
{
	
	public WebUserTest()
	{
	}

	/**
	 * Test of openNewWindow method, of class WebUser.
	 */
	@Test
	public void testOpenNewWindow()
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		
		WebUser user = new WebUser(browser);
		user.openNewWindow();
		Mockito.verify(browser).openNewWindow();
	}

	/**
	 * Test of closeWindow method, of class WebUser.
	 */
	@Test
	public void testCloseWindow()
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		
		WebUser user = new WebUser(browser);
		user.closeWindow();
		Mockito.verify(window).close();
	}

	/**
	 * Test of switchToWindow method, of class WebUser.
	 */
	@Test
	public void testSwitchToWindow()
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.switchToWindow("Window")).thenReturn(window);
		
		WebUser user = new WebUser(browser);
		user.switchToWindow("Window");
		Mockito.verify(browser).switchToWindow(ArgumentCaptor.forClass(String.class).capture());
	}

	/**
	 * Test of selectButton method, of class WebUser.
	 */
	@Test
	public void testSelectButton()
	{
		AElement element = Mockito.mock(AElement.class);
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("xpath")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		
		WebUser user = new WebUser(browser);
		user.selectButton("xpath");
		Mockito.verify(element).click();
	}

	/**
	 * Test of enterValueInElement method, of class WebUser.
	 */
	@Test
	public void testEnterValueInElement()
	{
		AElement element = Mockito.mock(AElement.class);
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("xpath")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		
		WebUser user = new WebUser(browser);
		user.enterValueInElement("xpath", "Value");
		
		ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(element).enterText(valueCaptor.capture());
		
		assertEquals("Value", valueCaptor.getValue());
	}

	/**
	 * Test of getElementValue method, of class WebUser.
	 */
	@Test
	public void testGetElementValue()
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.getElementText()).thenReturn("Value");
		
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(page.getElement("xpath")).thenReturn(element);
		
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		
		WebUser user = new WebUser(browser);
		String elementValue = user.getElementValue("xpath");
		assertEquals("Value", elementValue);
	}
}

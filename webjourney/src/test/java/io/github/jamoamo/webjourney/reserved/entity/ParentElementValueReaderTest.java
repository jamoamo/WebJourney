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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.reserved.entity.ParentElementValueReader;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author James Amoore
 */
public class ParentElementValueReaderTest
{

	public ParentElementValueReaderTest()
	{
	}

	/**
	 * Test of getCurrentUrl method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetCurrentUrl() throws Exception
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentUrl()).thenReturn("https://current.url");

		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);

		AElement parentElement = Mockito.mock(AElement.class);
		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		String currentUrl = reader.getCurrentUrl();
		assertEquals("https://current.url", currentUrl);
	}

	/**
	 * Test of getElementText method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetElementText() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);

		AElement childElement = Mockito.mock(AElement.class);
		Mockito.when(childElement.getElementText()).thenReturn("Text");

		AElement parentElement = Mockito.mock(AElement.class);
		Mockito.when(parentElement.findElement("xpath")).thenReturn(childElement);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		String text = reader.getElementText("xpath", false);
		assertEquals("Text", text);
	}

	/**
	 * Test of getElement method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetElement() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);

		AElement childElement = Mockito.mock(AElement.class);
		Mockito.when(childElement.getElementText()).thenReturn("Text");

		AElement parentElement = Mockito.mock(AElement.class);
		Mockito.when(parentElement.findElement("xpath")).thenReturn(childElement);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		AElement result = reader.getElement("xpath", false);
		assertSame(childElement, result);
	}

	/**
	 * Test of getAttribute method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetAttribute() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);

		AElement childElement = Mockito.mock(AElement.class);
		Mockito.when(childElement.getElementText()).thenReturn("Text");
		Mockito.when(childElement.getAttribute("attr")).thenReturn("attrVal");

		AElement parentElement = Mockito.mock(AElement.class);
		Mockito.when(parentElement.findElement("xpath")).thenReturn(childElement);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		String result = reader.getAttribute("xpath", "attr");
		assertEquals("attrVal", result);
	}

	/**
	 * Test of navigateTo method, of class ParentElementValueReader.
	 */
	@Test
	public void testNavigateTo()
		throws Exception
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentUrl()).thenReturn("https://current.url");

		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);

		AElement parentElement = Mockito.mock(AElement.class);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		reader.navigateTo(new URL("https://new.url"));

		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(window).navigateToUrl(urlCaptor.capture());

		assertEquals("https://new.url", urlCaptor.getValue().toString());
	}

	/**
	 * Test of navigateBack method, of class ParentElementValueReader.
	 */
	@Test
	public void testNavigateBack()
		throws Exception
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		Mockito.when(window.getCurrentUrl()).thenReturn("https://current.url");

		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);

		AElement parentElement = Mockito.mock(AElement.class);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		reader.navigateBack();

		Mockito.verify(window).navigateBack();
	}

	/**
	 * Test of getElements method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetElements() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);

		AElement childElement = Mockito.mock(AElement.class);
		Mockito.when(childElement.getElementText()).thenReturn("Text");

		AElement parentElement = Mockito.mock(AElement.class);

		Answer<List<AElement>> answer = (InvocationOnMock invocation) -> Collections.singletonList(childElement);
		
		Mockito.when(parentElement.findElements("xpath")).then(answer);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		List<? extends AElement> result = reader.getElements("xpath");
		assertEquals(1, result.size());
		assertSame(childElement, result.get(0));
	}

	/**
	 * Test of getElementTexts method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetElementTexts() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);

		AElement childElement = Mockito.mock(AElement.class);
		Mockito.when(childElement.getElementText()).thenReturn("Text");

		AElement parentElement = Mockito.mock(AElement.class);

		Answer<List<AElement>> answer = (InvocationOnMock invocation) -> Collections.singletonList(childElement);
		
		Mockito.when(parentElement.findElements("xpath")).then(answer);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		List<? extends AElement> result = reader.getElements("xpath");
		assertEquals(1, result.size());
		assertSame(childElement, result.get(0));
	}

	/**
	 * Test of getBrowser method, of class ParentElementValueReader.
	 */
	@Test
	public void testGetBrowser() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		AElement parentElement = Mockito.mock(AElement.class);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		IBrowser result = reader.getBrowser();
		assertSame(browser, result);
	}

	/**
	 * Test of openNewWindow method, of class ParentElementValueReader.
	 */
	@Test
	public void testOpenNewWindow() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		AElement parentElement = Mockito.mock(AElement.class);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		reader.openNewWindow();
		
		Mockito.verify(browser).openNewWindow();
	}

	/**
	 * Test of closeWindow method, of class ParentElementValueReader.
	 */
	@Test
	public void testCloseWindow() throws Exception
	{
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		IBrowser browser = Mockito.mock(IBrowser.class);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		
		AElement parentElement = Mockito.mock(AElement.class);

		ParentElementValueReader reader = new ParentElementValueReader(browser, parentElement);

		reader.closeWindow();
		
		Mockito.verify(window).close();
	}

}

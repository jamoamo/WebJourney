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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.ClickButtonAction;
import io.github.jamoamo.webjourney.JourneyContext;
import io.github.jamoamo.webjourney.ActionResult;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;

/**
 *
 * @author James Amoore
 */
public class ClickButtonActionTest
{
	
	public ClickButtonActionTest()
	{
	}

	/**
	 * Test of executeAction method, of class ClickButtonAction.
	 */
	@Test
	public void testExecuteAction_class_buttonExists() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		IWebPage page = Mockito.mock(IWebPage.class);
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		Mockito.when(page.getElement("//a[@data-cc-event='click:dismiss']")).thenReturn(element);
		JourneyContext context = new JourneyContext();
		context.setBrowser(browser);
		ClickButtonAction action = new ClickButtonAction(InputForm.class, "dismissBannerButton");
		ActionResult result = action.executeAction(context);
		assertEquals(ActionResult.SUCCESS, result);
		
		Mockito.verify(element, times(1)).click();
	}
	
	@Test
	public void testExecuteAction_object_buttonExists() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		IWebPage page = Mockito.mock(IWebPage.class);
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		Mockito.when(page.getElement("//a[@data-cc-event='click:dismiss']")).thenReturn(element);
		JourneyContext context = new JourneyContext();
		context.setBrowser(browser);
		
		InputForm form = new InputForm(2);
		ClickButtonAction action = new ClickButtonAction(form, "dismissBannerButton");
		ActionResult result = action.executeAction(context);
		assertEquals(ActionResult.SUCCESS, result);
		
		Mockito.verify(element, times(1)).click();
	}
	
	/**
	 * Test of executeAction method, of class ClickButtonAction.
	 */
	@Test
	public void testExecuteAction_class_buttonDoesntExist() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		JourneyContext context = new JourneyContext();
		context.setBrowser(browser);
		
		ClickButtonAction action = new ClickButtonAction(InputForm.class, "nonExistentButton");
		ActionResult result = action.executeAction(context);
		assertEquals(ActionResult.FAILURE, result);
	}
	
	/**
	 * Test of executeAction method, of class ClickButtonAction.
	 */
	@Test
	public void testExecuteAction_class_buttonNotAnnotated() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		IWebPage page = Mockito.mock(IWebPage.class);
		Mockito.when(window.getCurrentPage()).thenReturn(page);
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		JourneyContext context = new JourneyContext();
		context.setBrowser(browser);
		
		ClickButtonAction action = new ClickButtonAction(InputForm.class, "notAnnotatedField");
		ActionResult result = action.executeAction(context);
		assertEquals(ActionResult.FAILURE, result);
	}
}

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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IWebJourneyPath;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.URL;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class ConditionalActionTest
{
	
	public ConditionalActionTest()
	{
	}

	/**
	 * Test of executeActionImpl method, of class ConditionalAction.
	 */
	@Test
	public void testExecuteActionImpl_noFalse_true() throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);
		
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		
		Function<IBrowser, Boolean> conditionFunction = (browser) -> true;
		
		Function<IJourneyBuilder, IJourney> ifTrue = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.ifcalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		ConditionalAction instance = new ConditionalAction(conditionFunction, ifTrue);
		ActionResult result = instance.executeActionImpl(context);
		
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		
		Mockito.verify(mockWindow, Mockito.atMostOnce()).navigateToUrl(urlCaptor.capture());
		assertEquals(ActionResult.SUCCESS, result);
		assertEquals("https://www.ifcalled.com", urlCaptor.getValue().toString());
	}
	
	@Test
	public void testExecuteActionImpl_noFalse_false() throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);
		
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		
		Function<IBrowser, Boolean> conditionFunction = (browser) -> false;
		
		Function<IJourneyBuilder, IJourney> ifTrue = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.ifcalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		ConditionalAction instance = new ConditionalAction(conditionFunction, ifTrue);
		ActionResult result = instance.executeActionImpl(context);
		
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		
		Mockito.verify(mockWindow, Mockito.never()).navigateToUrl(urlCaptor.capture());
		assertEquals(ActionResult.SUCCESS, result);
	}
	
	@Test
	public void testExecuteActionImpl_false_true() throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);
		
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		
		Function<IBrowser, Boolean> conditionFunction = (browser) -> true;
		
		Function<IJourneyBuilder, IJourney> ifTrue = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.ifcalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		Function<IJourneyBuilder, IJourney> ifFalse = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.elsecalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		ConditionalAction instance = new ConditionalAction(conditionFunction, ifTrue, ifFalse);
		ActionResult result = instance.executeActionImpl(context);
		
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		
		Mockito.verify(mockWindow, Mockito.atMostOnce()).navigateToUrl(urlCaptor.capture());
		assertEquals(ActionResult.SUCCESS, result);
		assertEquals("https://www.ifcalled.com", urlCaptor.getValue().toString());
	}
	
	@Test
	public void testExecuteActionImpl_false_false() throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);
		
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		
		Function<IBrowser, Boolean> conditionFunction = (browser) -> false;
		
		Function<IJourneyBuilder, IJourney> ifTrue = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.ifcalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		Function<IJourneyBuilder, IJourney> ifFalse = (builder) -> {
			try
			{
				return builder.navigateTo("https://www.elsecalled.com").build();
			}
			catch(Exception ex)
			 {
				 fail(ex);
			 }
			return null;
		};
		
		ConditionalAction instance = new ConditionalAction(conditionFunction, ifTrue, ifFalse);
		ActionResult result = instance.executeActionImpl(context);
		
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		
		Mockito.verify(mockWindow, Mockito.atMostOnce()).navigateToUrl(urlCaptor.capture());
		assertEquals(ActionResult.SUCCESS, result);
		assertEquals("https://www.elsecalled.com", urlCaptor.getValue().toString());
	}

	/**
	 * Test of getActionName method, of class ConditionalAction.
	 */
	@Test
	public void testGetActionName()
	{
		ConditionalAction instance = new ConditionalAction(null, null, null);
		String expResult = "Conditional";
		String result = instance.getActionName();
		assertEquals(expResult, result);
	}
	
}

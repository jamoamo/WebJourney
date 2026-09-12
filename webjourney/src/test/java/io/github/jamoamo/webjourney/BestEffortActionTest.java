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

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import io.github.jamoamo.webjourney.api.web.XWebException;
import io.github.jamoamo.webjourney.reserved.JourneyBreadcrumb;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class BestEffortActionTest
{
	private IJourneyContext newContext(IBrowserWindow mockWindow)
	{
		IBrowser mockBrowser = Mockito.mock(IBrowser.class);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);

		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		Mockito.when(context.getBrowser()).thenReturn(mockBrowser);
		Mockito.when(context.getJourneyBreadcrumb()).thenReturn(new JourneyBreadcrumb());
		return context;
	}

	@Test
	public void testExecuteActionImpl_subJourneySucceeds_runsItAndReturnsSuccess()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);

		BestEffortAction instance = new BestEffortAction(
			builder -> builder.navigateTo("https://www.ifcalled.com").build());

		ActionResult result = instance.executeActionImpl(context);

		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(mockWindow).navigateToUrl(urlCaptor.capture());
		assertEquals("https://www.ifcalled.com", urlCaptor.getValue().toString());
		assertEquals(ActionResult.SUCCESS, result);
	}

	@Test
	public void testExecuteActionImpl_subJourneyThrows_isSwallowedAndReturnsSuccess()
		throws XNavigationError, XWebException
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		Mockito.doThrow(new XNavigationError("navigation broke"))
			.when(mockWindow)
			.navigateToUrl(Mockito.any());
		IJourneyContext context = newContext(mockWindow);

		BestEffortAction instance = new BestEffortAction(
			builder -> builder.navigateTo("https://www.broken.com").build());

		ActionResult result = instance.executeActionImpl(context);

		assertEquals(ActionResult.SUCCESS, result);
	}

	@Test
	public void testExecuteActionImpl_subJourneySupplierThrows_isSwallowedAndReturnsSuccess()
	{
		IBrowserWindow mockWindow = Mockito.mock(IBrowserWindow.class);
		IJourneyContext context = newContext(mockWindow);

		BestEffortAction instance = new BestEffortAction(builder ->
		{
			throw new JourneyException("could not build sub journey", new JourneyBreadcrumb());
		});

		ActionResult result = instance.executeActionImpl(context);

		assertEquals(ActionResult.SUCCESS, result);
	}

	@Test
	public void testGetActionName()
	{
		BestEffortAction instance = new BestEffortAction(builder -> builder.build());
		assertEquals("BestEffort", instance.getActionName());
	}
}

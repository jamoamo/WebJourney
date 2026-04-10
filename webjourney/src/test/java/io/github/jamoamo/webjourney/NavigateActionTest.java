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

import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XWebException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class NavigateActionTest
{

	 public NavigateActionTest()
	 {
	 }

	 /**
	  * Test of executeAction method, of class NavigateAction.
	  */
	 @Test
	 public void testExecuteAction()
		  throws XWebException
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  NavigateAction action = new NavigateAction(NavigationTarget.back());
		  action.executeAction(context);
		  Mockito.verify(window)
				.navigateBack();
	 }

	 /**
	  * Test of executeAction method with a retry policy triggering on failure.
	  */
	 @Test
	 public void testExecuteAction_withRetry()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  io.github.jamoamo.webjourney.api.ITravelOptions options = new io.github.jamoamo.webjourney.TravelOptions();
		  options.setRetryPolicy(io.github.jamoamo.webjourney.api.RetryPolicyBuilder.builder()
			  .maxRetries(3).delay(java.time.Duration.ofMillis(10)).build());
		  context.setOptions(options);

		  IWebPage page = Mockito.mock(IWebPage.class);
		  Mockito.when(window.navigateBack())
				.thenThrow(new XWebException("Transient error"))
				.thenReturn(page);

		  NavigateAction action = new NavigateAction(NavigationTarget.back());
		  action.executeAction(context);
		  
		  Mockito.verify(window, Mockito.times(2)).navigateBack();
	 }

}

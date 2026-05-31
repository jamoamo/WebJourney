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
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class WaitUntilUrlChangesActionTest
{

	 public WaitUntilUrlChangesActionTest()
	 {
	 }

	 /**
	  * Test of executeAction method when the url has already changed away from the substring.
	  */
	 @Test
	 public void testExecuteAction_urlAlreadyChanged()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(window.getCurrentUrl())
				.thenReturn("https://example.com/match.cgi");
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  WaitUntilUrlChangesAction action =
				new WaitUntilUrlChangesAction("intermediate.cgi", Duration.ofSeconds(1));
		  ActionResult result = action.executeAction(context);
		  assertEquals(ActionResult.SUCCESS, result);
	 }

	 /**
	  * Test of executeAction method when the url changes away from the substring after polling.
	  */
	 @Test
	 public void testExecuteAction_urlChangesAfterPolling()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(window.getCurrentUrl())
				.thenReturn("https://example.com/intermediate.cgi")
				.thenReturn("https://example.com/intermediate.cgi")
				.thenReturn("https://example.com/match.cgi");
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  WaitUntilUrlChangesAction action =
				new WaitUntilUrlChangesAction("intermediate.cgi", Duration.ofSeconds(5));
		  ActionResult result = action.executeAction(context);
		  assertEquals(ActionResult.SUCCESS, result);

		  Mockito.verify(window, Mockito.times(3))
				.getCurrentUrl();
	 }

	 /**
	  * Test of executeAction method when the url never changes and the timeout is exceeded.
	  */
	 @Test
	 public void testExecuteAction_timeout()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(window.getCurrentUrl())
				.thenReturn("https://example.com/intermediate.cgi");
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  WaitUntilUrlChangesAction action =
				new WaitUntilUrlChangesAction("intermediate.cgi", Duration.ofMillis(300));
		  assertThrows(BaseJourneyActionException.class, () -> action.executeAction(context));
	 }

	 /**
	  * Test of executeAction method when retrieving the url throws a browsing error.
	  */
	 @Test
	 public void testExecuteAction_urlError()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(window.getCurrentUrl())
				.thenThrow(new XWebException("Browsing error"));
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  WaitUntilUrlChangesAction action =
				new WaitUntilUrlChangesAction("intermediate.cgi", Duration.ofSeconds(1));
		  assertThrows(BaseJourneyActionException.class, () -> action.executeAction(context));
	 }

}

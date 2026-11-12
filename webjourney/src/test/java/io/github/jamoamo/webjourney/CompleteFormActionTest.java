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

import io.github.jamoamo.webjourney.api.RetryPolicyBuilder;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class CompleteFormActionTest
{

	 public CompleteFormActionTest()
	 {
	 }

	 @Test
	 public void testExecuteAction_noSubmit()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  IWebPage page = Mockito.mock(IWebPage.class);
		  AElement element = Mockito.mock(AElement.class);
		  Mockito.when(window.getCurrentPage())
				.thenReturn(page);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(page.getElement("//input[@name='match']"))
				.thenReturn(element);
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  CompleteFormAction action = new CompleteFormAction(new InputForm(1));
		  ActionResult result = action.executeAction(context);
		  assertEquals(ActionResult.SUCCESS, result);

		  ArgumentCaptor<String> valueArg = ArgumentCaptor.forClass(String.class);
		  Mockito.verify(element, Mockito.times(1))
				.enterText(valueArg.capture());

		  assertEquals("t1", valueArg.getValue());
	 }

	 @Test
	 public void testExecuteAction_submit()
		  throws Exception
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  IWebPage page = Mockito.mock(IWebPage.class);
		  AElement element = Mockito.mock(AElement.class);
		  AElement buttonElement = Mockito.mock(AElement.class);
		  Mockito.when(window.getCurrentPage())
				.thenReturn(page);
		  Mockito.when(browser.getActiveWindow())
				.thenReturn(window);
		  Mockito.when(page.getElement("//input[@name='match']"))
				.thenReturn(element);
		  Mockito.when(page.getElement("//form[@name='scorecard_oracle_form']//table//tbody//input[@type='submit']"))
				.thenReturn(buttonElement);

		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);

		  CompleteFormAction action = new CompleteFormAction(new InputForm(1));
		  action.setSubmitAfterwards();

		  ActionResult result = action.executeAction(context);
		  assertEquals(ActionResult.SUCCESS, result);

		  ArgumentCaptor<String> valueArg = ArgumentCaptor.forClass(String.class);
		  Mockito.verify(element, Mockito.times(1))
				.enterText(valueArg.capture());
		  Mockito.verify(buttonElement)
				.click();

		  assertEquals("t1", valueArg.getValue());
	 }

	 @Test
	 public void testExecuteAction_noRetryAnnotation_doesNotRefresh()
		  throws Exception
	 {
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  IWebPage page = Mockito.mock(IWebPage.class);
		  Mockito.when(window.getCurrentPage()).thenReturn(page);
		  Mockito.when(page.getElement(MATCH_XPATH)).thenThrow(new XElementDoesntExistException());

		  CompleteFormAction action = new CompleteFormAction(new InputForm(1));

		  assertThrows(Exception.class, () -> action.executeAction(contextWithoutActionRetry(window)));
		  Mockito.verify(page, Mockito.times(1)).getElement(MATCH_XPATH);
		  Mockito.verify(window, Mockito.never()).refreshCurrentPage();
	 }

	 @Test
	 public void testExecuteAction_retryAnnotation_refreshesAndSucceeds()
		  throws Exception
	 {
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  IWebPage page = Mockito.mock(IWebPage.class);
		  AElement element = Mockito.mock(AElement.class);
		  AElement buttonElement = Mockito.mock(AElement.class);
		  Mockito.when(window.getCurrentPage()).thenReturn(page);
		  Mockito.when(page.getElement(MATCH_XPATH))
				.thenThrow(new XElementDoesntExistException())
				.thenReturn(element);
		  Mockito.when(page.getElement(SUBMIT_XPATH)).thenReturn(buttonElement);

		  CompleteFormAction action = new CompleteFormAction(new RetryingInputForm(1));
		  action.setSubmitAfterwards();

		  ActionResult result = action.executeAction(contextWithoutActionRetry(window));

		  assertEquals(ActionResult.SUCCESS, result);
		  Mockito.verify(window, Mockito.times(1)).refreshCurrentPage();
		  Mockito.verify(element, Mockito.times(1)).enterText("t1");
		  Mockito.verify(buttonElement, Mockito.times(1)).click();
	 }

	 @Test
	 public void testExecuteAction_retryAnnotation_exhaustsRetries()
		  throws Exception
	 {
		  IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		  IWebPage page = Mockito.mock(IWebPage.class);
		  Mockito.when(window.getCurrentPage()).thenReturn(page);
		  Mockito.when(page.getElement(MATCH_XPATH)).thenThrow(new XElementDoesntExistException());

		  CompleteFormAction action = new CompleteFormAction(new RetryingInputForm(1));

		  assertThrows(Exception.class, () -> action.executeAction(contextWithoutActionRetry(window)));
		  // maxRetries = 2: three attempts, the last two each after a reload
		  Mockito.verify(page, Mockito.times(3)).getElement(MATCH_XPATH);
		  Mockito.verify(window, Mockito.times(2)).refreshCurrentPage();
	 }

	 private static final String MATCH_XPATH = "//input[@name='match']";
	 private static final String SUBMIT_XPATH =
		  "//form[@name='scorecard_oracle_form']//table//tbody//input[@type='submit']";

	 /**
	  * A context whose action-level retry policy does not retry, so only the form's own retries are counted.
	  */
	 private static JourneyContext contextWithoutActionRetry(IBrowserWindow window)
	 {
		  IBrowser browser = Mockito.mock(IBrowser.class);
		  Mockito.when(browser.getActiveWindow()).thenReturn(window);
		  TravelOptions options = new TravelOptions();
		  options.setRetryPolicy(RetryPolicyBuilder.builder().maxRetries(0).build());
		  JourneyContext context = new JourneyContext();
		  context.setBrowser(browser);
		  context.setOptions(options);
		  return context;
	 }

}

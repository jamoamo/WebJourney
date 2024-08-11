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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.IWebPage;
import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author James Amoore
 */
public class SeleniumWindowTest
{
	 RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);

	 /**
	  * Test of getWindowName method, of class SeleniumWindow.
	  */
	 @Test
	 public void testGetName()
	 {
		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  String name = window.getName();
		  assertEquals("Name", name);
	 }

	 /**
	  * Test of getCurrentUrl method, of class SeleniumWindow.
	  */
	 @Test
	 public void testGetCurrentUrl_active()
	 {
		  Mockito.when(driverMock.getCurrentUrl())
				.thenReturn("http://current.url");

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  String url = window.getCurrentUrl();
		  assertEquals("http://current.url", url);
	 }

	 @Test
	 public void testGetCurrentUrl_inactive()
	 {
		  Mockito.when(driverMock.getCurrentUrl())
				.thenReturn("http://current.url");

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, () -> window.getCurrentUrl());
	 }

	 /**
	  * Test of getCurrentPage method, of class SeleniumWindow.
	  */
	 @Test
	 public void testGetCurrentPage_active()
	 {
		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  IWebPage page = window.getCurrentPage();
		  assertNotNull(page);
	 }

	 @Test
	 public void testGetCurrentPage_inactive()
	 {
		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, () -> window.getCurrentPage());
	 }

	 /**
	  * Test of refreshCurrentPage method, of class SeleniumWindow.
	  */
	 @Test
	 public void testRefreshCurrentPage_active()
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  IWebPage page = window.refreshCurrentPage();
		  assertNotNull(page);

		  verify(navigate)
				.refresh();
	 }

	 @Test
	 public void testRefreshCurrentPage_inactive()
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, () -> window.refreshCurrentPage());
	 }

	 /**
	  * Test of close method, of class SeleniumWindow.
	  */
	 @Test
	 public void testClose_active()
	 {
		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  window.close();

		  verify(driverMock)
				.close();
	 }

	 @Test
	 public void testClose_inactive()
	 {
		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, () -> window.close());
	 }

	 /**
	  * Test of navigateToUrl method, of class SeleniumWindow.
	  */
	 @Test
	 public void testNavigateToUrl_active()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);
		  Mockito.when(driverMock.getScreenshotAs(OutputType.FILE))
				.thenReturn(new File(""));

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  IWebPage page = window.navigateToUrl(new URL("https://new.url"));
		  assertNotNull(page);

		  ArgumentCaptor<URL> captor = ArgumentCaptor.forClass(URL.class);
		  verify(navigate)
				.to(captor.capture());
		  assertEquals("https://new.url", captor.getValue()
				.toString());
	 }

	 @Test
	 public void testNavigateToUrl_inactive()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, () -> window.navigateToUrl(new URL("https://new.url")));
	 }

	 /**
	  * Test of navigateBack method, of class SeleniumWindow.
	  */
	 @Test
	 public void testNavigateBack_active()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  IWebPage page = window.navigateBack();
		  assertNotNull(page);

		  verify(navigate)
				.back();
	 }

	 @Test
	 public void testNavigateBack_inactive()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);
		  assertThrows(XInactiveWindowException.class, window::navigateBack);
	 }

	 /**
	  * Test of navigateForward method, of class SeleniumWindow.
	  */
	 @Test
	 public void testNavigateForward_active()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(true);
		  IWebPage page = window.navigateForward();
		  assertNotNull(page);

		  verify(navigate)
				.forward();
	 }

	 @Test
	 public void testNavigateForward_inactive()
		  throws Exception
	 {
		  Navigation navigate = Mockito.mock(Navigation.class);

		  Mockito.when(driverMock.navigate())
				.thenReturn(navigate);

		  SeleniumWindow window = new SeleniumWindow("Name", driverMock);
		  window.setActive(false);

		  assertThrows(XInactiveWindowException.class, window::navigateForward);
	 }

}

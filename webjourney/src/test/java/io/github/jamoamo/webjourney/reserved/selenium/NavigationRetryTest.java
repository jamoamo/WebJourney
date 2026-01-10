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
import io.github.jamoamo.webjourney.api.web.XEnvironmentalNavigationError;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Tests for navigation retry functionality.
 * 
 * @author James Amoore
 */
public class NavigationRetryTest
{
	/**
	 * Test that a successful navigation on first attempt works normally.
	 */
	@Test
	public void testSuccessfulNavigationFirstAttempt() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 100));
		
		IWebPage page = window.navigateToUrl(URI.create("https://example.com").toURL());
		
		assertNotNull(page);
		verify(navigate, times(1)).to(any(URL.class));
	}
	
	/**
	 * Test that environmental errors trigger retry and succeed on second attempt.
	 */
	@Test
	public void testEnvironmentalErrorRetrySuccess() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Fail first attempt with connection refused, succeed on second
		Mockito.doThrow(new WebDriverException("unknown error: net::ERR_CONNECTION_REFUSED"))
			.doNothing()
			.when(navigate).to(any(URL.class));
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10)); // Short delay for testing
		
		IWebPage page = window.navigateToUrl(URI.create("https://example.com").toURL());
		
		assertNotNull(page);
		verify(navigate, times(2)).to(any(URL.class));
	}
	
	/**
	 * Test that environmental errors are retried up to max attempts.
	 */
	@Test
	public void testEnvironmentalErrorMaxRetries() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Always fail with connection refused
		Mockito.doThrow(new WebDriverException("unknown error: net::ERR_CONNECTION_REFUSED"))
			.when(navigate).to(any(URL.class));
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10)); // Short delay for testing
		
		XEnvironmentalNavigationError exception = assertThrows(
			XEnvironmentalNavigationError.class,
			() -> window.navigateToUrl(URI.create("https://example.com").toURL())
		);
		
		assertEquals(3, exception.getAttemptsMade());
		verify(navigate, times(3)).to(any(URL.class));
	}
	
	/**
	 * Test that non-environmental errors are not retried.
	 */
	@Test
	public void testNonEnvironmentalErrorNoRetry() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Fail with a non-environmental error
		Mockito.doThrow(new WebDriverException("element not found"))
			.when(navigate).to(any(URL.class));
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10));
		
		assertThrows(
			XNavigationError.class,
			() -> window.navigateToUrl(URI.create("https://example.com").toURL())
		);
		
		// Should only be called once (no retry)
		verify(navigate, times(1)).to(any(URL.class));
	}
	
	/**
	 * Test retry logic for navigateBack operation.
	 */
	@Test
	public void testNavigateBackRetry() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Fail first, succeed second
		Mockito.doThrow(new WebDriverException("net::ERR_CONNECTION_TIMED_OUT"))
			.doNothing()
			.when(navigate).back();
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10));
		
		IWebPage page = window.navigateBack();
		
		assertNotNull(page);
		verify(navigate, times(2)).back();
	}
	
	/**
	 * Test retry logic for navigateForward operation.
	 */
	@Test
	public void testNavigateForwardRetry() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Fail first, succeed second
		Mockito.doThrow(new WebDriverException("net::ERR_NETWORK_CHANGED"))
			.doNothing()
			.when(navigate).forward();
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10));
		
		IWebPage page = window.navigateForward();
		
		assertNotNull(page);
		verify(navigate, times(2)).forward();
	}
	
	/**
	 * Test retry logic for refreshCurrentPage operation.
	 */
	@Test
	public void testRefreshCurrentPageRetry() throws Exception
	{
		RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
		Navigation navigate = Mockito.mock(Navigation.class);
		
		Mockito.when(driverMock.navigate()).thenReturn(navigate);
		
		// Fail first, succeed second
		Mockito.doThrow(new WebDriverException("net::ERR_INTERNET_DISCONNECTED"))
			.doNothing()
			.when(navigate).refresh();
		
		SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
		window.setActive(true);
		window.setRetryConfig(new NavigationRetryConfig(3, 10));
		
		IWebPage page = window.refreshCurrentPage();
		
		assertNotNull(page);
		verify(navigate, times(2)).refresh();
	}
	
	/**
	 * Test various environmental error patterns are detected.
	 */
	@Test
	public void testEnvironmentalErrorPatternDetection() throws Exception
	{
		String[] environmentalErrors = {
			"net::ERR_CONNECTION_REFUSED",
			"net::ERR_CONNECTION_RESET",
			"net::ERR_CONNECTION_CLOSED",
			"net::ERR_CONNECTION_TIMED_OUT",
			"net::ERR_NETWORK_CHANGED",
			"net::ERR_INTERNET_DISCONNECTED",
			"net::ERR_TIMED_OUT",
			"net::ERR_NAME_NOT_RESOLVED",
			"net::ERR_PROXY_CONNECTION_FAILED"
		};
		
		for (String errorMessage : environmentalErrors)
		{
			RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
			Navigation navigate = Mockito.mock(Navigation.class);
			
			Mockito.when(driverMock.navigate()).thenReturn(navigate);
			
			// Fail with environmental error, then succeed
			Mockito.doThrow(new WebDriverException(errorMessage))
				.doNothing()
				.when(navigate).to(any(URL.class));
			
			SeleniumWindow window = new SeleniumWindow("TestWindow", driverMock);
			window.setActive(true);
			window.setRetryConfig(new NavigationRetryConfig(3, 10));
			
			IWebPage page = window.navigateToUrl(URI.create("https://example.com").toURL());
			
			assertNotNull(page, "Should have succeeded after retry for error: " + errorMessage);
			verify(navigate, times(2)).to(any(URL.class));
		}
	}
	
	/**
	 * Test NavigationRetryConfig validation.
	 */
	@Test
	public void testRetryConfigValidation()
	{
		// Valid configs
		assertDoesNotThrow(() -> new NavigationRetryConfig(1, 0));
		assertDoesNotThrow(() -> new NavigationRetryConfig(5, 1000));
		
		// Invalid configs
		assertThrows(IllegalArgumentException.class, () -> new NavigationRetryConfig(0, 100));
		assertThrows(IllegalArgumentException.class, () -> new NavigationRetryConfig(-1, 100));
		assertThrows(IllegalArgumentException.class, () -> new NavigationRetryConfig(3, -1));
	}
	
	/**
	 * Test default retry configuration.
	 */
	@Test
	public void testDefaultRetryConfig()
	{
		NavigationRetryConfig config = new NavigationRetryConfig();
		assertEquals(3, config.getMaxAttempts());
		assertEquals(1000, config.getRetryDelayMillis());
	}
}


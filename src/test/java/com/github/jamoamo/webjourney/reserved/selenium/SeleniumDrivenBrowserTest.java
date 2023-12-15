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
package com.github.jamoamo.webjourney.reserved.selenium;

import com.github.jamoamo.webjourney.api.web.IBrowserWindow;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author James Amoore
 */
public class SeleniumDrivenBrowserTest
{
	private static RemoteWebDriver driverMock = Mockito.mock(RemoteWebDriver.class);
	private static TargetLocator targetLocatorMock = Mockito.mock(TargetLocator.class);
	
	@BeforeAll
	public static void setup()
	{
		Timeouts timeoutsMock = Mockito.mock(Timeouts.class);
		
		Options optionsMock = Mockito.mock(Options.class);
		Mockito.when(optionsMock.timeouts()).thenReturn(timeoutsMock);
		
		Mockito.when(driverMock.manage()).thenReturn(optionsMock);
		Mockito.when(driverMock.switchTo()).thenReturn(targetLocatorMock);
		Mockito.when(driverMock.getWindowHandle()).thenReturn("Window1");
	}

	/**
	 * Test of getActiveWindow method, of class SeleniumDrivenBrowser.
	 */
	@Test
	public void testGetActiveWindow()
	{
		Mockito.when(driverMock.getWindowHandle()).thenReturn("Window1", "Window2");
		
		SeleniumDrivenBrowser seleniumBrowser = new SeleniumDrivenBrowser(driverMock);
		seleniumBrowser.openNewWindow();
		IBrowserWindow activeWindow = seleniumBrowser.getActiveWindow();
		assertNotNull(activeWindow);
		assertEquals("Window2", activeWindow.getName());
		assertTrue(((SeleniumWindow)activeWindow).isActive());
	}

	/**
	 * Test of switchToWindow method, of class SeleniumDrivenBrowser.
	 */
	@Test
	public void testSwitchToWindow()
	{
		Mockito.when(driverMock.getWindowHandle()).thenReturn("Window1", "Window2");
		
		SeleniumDrivenBrowser seleniumBrowser = new SeleniumDrivenBrowser(driverMock);
		seleniumBrowser.openNewWindow();
		IBrowserWindow switchedWindow = seleniumBrowser.switchToWindow("Window1");
		assertNotNull(switchedWindow);
		assertEquals("Window1", switchedWindow.getName());
			
		Mockito.verify(targetLocatorMock).window(any());
	}

	/**
	 * Test of openNewWindow method, of class SeleniumDrivenBrowser.
	 */
	@Test
	public void testOpenNewWindow()
	{
		Mockito.when(driverMock.getWindowHandle()).thenReturn("Window1", "Window2");
		
		SeleniumDrivenBrowser seleniumBrowser = new SeleniumDrivenBrowser(driverMock);
		IBrowserWindow switchedWindow = seleniumBrowser.openNewWindow();
		assertNotNull(switchedWindow);
		assertEquals("Window2", switchedWindow.getName());
		assertTrue(((SeleniumWindow)switchedWindow).isActive());
	}

	/**
	 * Test of exit method, of class SeleniumDrivenBrowser.
	 */
	@Test
	public void testExit()
	{
		Mockito.when(driverMock.getWindowHandle()).thenReturn("Window1");
		
		SeleniumDrivenBrowser seleniumBrowser = new SeleniumDrivenBrowser(driverMock);
		seleniumBrowser.exit();
		
		verify(driverMock).quit();
	}
	
}

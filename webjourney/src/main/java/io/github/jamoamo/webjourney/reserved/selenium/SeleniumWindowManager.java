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

import java.util.HashMap;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class SeleniumWindowManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWindowManager.class);
	
	private final HashMap<String, SeleniumWindow> windowNames = new HashMap<>();
	private final RemoteWebDriver webDriver;
	
	SeleniumWindowManager(RemoteWebDriver webDriver)
	{
		this.webDriver = webDriver;
		String windowHandle = this.webDriver.getWindowHandle();
		SeleniumWindow window = new SeleniumWindow(windowHandle, this.webDriver);
		this.windowNames.put(windowHandle, window);
		LOGGER.info("Starting browser window has handle " + windowHandle);
		window.setActive(true);
	}
	
	SeleniumWindow switchToWindow(String windowHandle)
	{
		
		//first deactivate existing active window
		getActiveWindow().setActive(false);
		//switch to new window
		this.webDriver.switchTo().window(windowHandle);
		LOGGER.info("Switched to window with handle " + windowHandle);
		//set it active
		SeleniumWindow window = this.windowNames.get(windowHandle);
		window.setActive(true);
		return window;
	}

	SeleniumWindow getActiveWindow()
	{
		return this.windowNames.values().stream().filter(window -> window.isActive()).findFirst().get();
	}

	SeleniumWindow openNewWindow()
	{
		//first deactivate existing active window
		getActiveWindow().setActive(false);
		this.webDriver.switchTo().newWindow(WindowType.TAB);
		SeleniumWindow window = new SeleniumWindow(this.webDriver.getWindowHandle(), this.webDriver);
		this.windowNames.put(window.getName(), window);
		LOGGER.info("Opened new window with handle " + window.getName());
		window.setActive(true);
		return window;
	}
}

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

import java.util.HashMap;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;

/**
 *
 * @author James Amoore
 */
class SeleniumWindowManager
{
	private final HashMap<String, SeleniumWindow> windowNames = new HashMap<>();
	private final WebDriver webDriver;
	
	SeleniumWindowManager(WebDriver webDriver)
	{
		this.webDriver = webDriver;
		String windowHandle = this.webDriver.getWindowHandle();
		SeleniumWindow window = new SeleniumWindow(windowHandle, this.webDriver);
		this.windowNames.put(windowHandle, window);
		window.setActive(true);
	}
	
	SeleniumWindow switchToWindow(String windowHandle)
	{
		//first deactivate existing active window
		getActiveWindow().setActive(false);
		//switch to new window
		this.webDriver.switchTo().window(windowHandle);
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
		window.setActive(true);
		return window;
	}
}

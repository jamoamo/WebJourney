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
package com.github.jamoamo.webjourney.api.web;

/**
 * A user of the web.
 * @author James Amoore
 */
final class WebUser implements IWebUser
{
	private final IBrowser browser;
	private IBrowserWindow currentWindow;
	
	WebUser(IBrowser browser)
	{
		this.browser = browser;
		this.currentWindow = browser.getActiveWindow();
	}
	
	@Override
	public void openNewWindow()
	{
		this.currentWindow = this.browser.openNewWindow();
	}

	@Override
	public void closeWindow()
	{
		this.currentWindow.close();
	}

	@Override
	public void switchToWindow(String windowName)
	{
		this.currentWindow = this.browser.switchToWindow(windowName);
	}

	@Override
	public void selectButton(String xPath)
	{
		this.currentWindow.getCurrentPage().getElement(xPath).click();
	}

	@Override
	public void enterValueInElement(String xPath, String value)
	{
		this.currentWindow.getCurrentPage().getElement(xPath).enterText(value);
	}

	@Override
	public String getElementValue(String xPath)
	{
		return this.currentWindow.getCurrentPage().getElement(xPath).getElementText();
	}
	
}

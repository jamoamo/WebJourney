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
import com.github.jamoamo.webjourney.api.web.IWebPage;
import com.github.jamoamo.webjourney.api.web.XNavigationError;
import java.net.URL;
import org.openqa.selenium.WebDriver;

/**
 *
 * @author James Amoore
 */
final class SeleniumWindow implements IBrowserWindow
{
	private final String windowName;
	private boolean active;
	private final WebDriver webDriver;
	private IWebPage currentPage;
	
	SeleniumWindow(String windowName, WebDriver webDriver)
	{
		this.windowName = windowName;
		this.webDriver = webDriver;
		this.currentPage = new SeleniumPage(this.webDriver);
		this.active = false;
	}
	
	private void checkWindowIsActive()
	{
		if(!this.active)
		{
			throw new XInactiveWindowException();
		}
	}

	@Override
	public String getCurrentUrl()
	{
		checkWindowIsActive();
		return this.webDriver.getCurrentUrl();
	}

	@Override
	public IWebPage getCurrentPage()
	{
		checkWindowIsActive();
		return this.currentPage;
	}

	@Override
	public IWebPage refreshCurrentPage()
	{
		checkWindowIsActive();
		this.webDriver.navigate().refresh();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public void close()
	{
		checkWindowIsActive();
		this.webDriver.close();
		this.currentPage = new SeleniumPage(this.webDriver);
	}

	@Override
	public IWebPage navigateToUrl(URL url)
		throws XNavigationError
	{
		checkWindowIsActive();
		this.webDriver.navigate().to(url);
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateBack()
		throws XNavigationError
	{
		checkWindowIsActive();
		this.webDriver.navigate().back();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateForward()
		throws XNavigationError
	{
		checkWindowIsActive();
		this.webDriver.navigate().forward();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}
	
	void setActive(boolean active)
	{
		this.active = active;
	}
	
	boolean isActive()
	{
		return this.active;
	}

	@Override
	public String getName()
	{
		return this.windowName;
	}
}

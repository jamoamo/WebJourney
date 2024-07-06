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

import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
final class SeleniumWindow implements IBrowserWindow
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWindow.class);
	private final String windowName;
	private boolean active;
	private final RemoteWebDriver webDriver;
	private IWebPage currentPage;
	
	private boolean screenshotEnabled = false;
	
	SeleniumWindow(String windowName, RemoteWebDriver webDriver)
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
		LOGGER.info(String.format("Window [%s] navigating to url %s", this.windowName, url.toString()));
		this.webDriver.navigate().to(url);
		
		takeScreenshot();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateBack()
		throws XNavigationError
	{
		checkWindowIsActive();
		LOGGER.info(String.format("Window [%s] navigating back", this.windowName));
		
		this.webDriver.navigate().back();
		takeScreenshot();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateForward()
		throws XNavigationError
	{
		checkWindowIsActive();
		LOGGER.info(String.format("Window [%s] navigating forward", this.windowName));
		
		this.webDriver.navigate().forward();
		takeScreenshot();
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
	
	private void takeScreenshot()
	{
		if(!this.screenshotEnabled)
		{
			return;
		}
		
		try
		{
			LOGGER.debug("Taking Screenshot");
			File srcFile = ((TakesScreenshot)this.webDriver).getScreenshotAs(OutputType.FILE);
			
			if(srcFile == null)
			{
				return;
			}
			
			LocalDateTime time = LocalDateTime.now();
			var timeStr = time.format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"));
			String fileName = String.format("output/screenshot/screenshot-%s.png", timeStr);
			File destFile = new File(fileName);
			LOGGER.debug(String.format("Copying Screenshot [%s]", fileName));
			FileUtils.copyFile(srcFile, destFile);
		}
		catch(IOException ioe)
		{
			LOGGER.error("Could not take screenshot.", ioe);
		}
	}
}

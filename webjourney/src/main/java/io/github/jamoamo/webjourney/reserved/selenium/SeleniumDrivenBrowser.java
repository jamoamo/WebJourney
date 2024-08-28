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

import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import java.time.Duration;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A browser that uses Selenium to drive the browser interactions.
 *
 * @author James Amoore
 */
class SeleniumDrivenBrowser implements IBrowser
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumDrivenBrowser.class);
	private static final int DEFAULT_TIMEOUT = 10;

	private final RemoteWebDriver driver;
	private final SeleniumWindowManager windowManager;
	
	private final String browserName;
	private final String browserVersion;

	SeleniumDrivenBrowser(RemoteWebDriver driver)
	{
		this.driver = driver;
		this.windowManager = new SeleniumWindowManager(this.driver);
		this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(DEFAULT_TIMEOUT));
		this.browserName = this.driver.getCapabilities().getBrowserName();
		this.browserVersion = this.driver.getCapabilities().getBrowserVersion();
		
		LOGGER.info(String.format("Using browser %s version %s", this.browserName, this.browserVersion));
	}

	@Override
	public IBrowserWindow getActiveWindow()
	{
		return this.windowManager.getActiveWindow();
	}

	@Override
	public IBrowserWindow switchToWindow(String windowName)
	{
		LOGGER.info(String.format("Switch to window [%s].", windowName));
		return this.windowManager.switchToWindow(windowName);
	}

	@Override
	public IBrowserWindow openNewWindow()
	{
		LOGGER.info(String.format("Openning new window."));
		return this.windowManager.openNewWindow();
	}

	@Override
	@SuppressWarnings("MagicNumber")
	public void exit()
	{
		LOGGER.info("Closing browser.");
		if("chrome".equals(this.browserName))
		{
		  int majorVersion = Integer.parseInt(this.browserVersion.split("[.]")[0]);
		  if(majorVersion <= 128 && majorVersion >= 123)
		  {
				this.driver.close();
		  }
		}
		this.driver.quit();
	}
}

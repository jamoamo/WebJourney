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

import io.github.jamoamo.webjourney.api.web.IBrowserFactory;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import java.util.Map;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Browser factory to create a chrome browser.
 *
 * @author James Amoore
 */
public final class ChromeBrowserFactory implements IBrowserFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ChromeBrowserFactory.class);
	/**
	 * Creates a new Chrome browser.
	 *
	 * @param browserOptions the options to use to create the browser
	 *
	 * @return a new Chrome browser instance.
	 */
	@Override
	public IBrowser createBrowser(IBrowserOptions browserOptions)
	{
		ChromeOptions options =
			createChromeOptions(browserOptions);
		
		logSeleniumBuildInfo();
		
		ChromeDriver driver = new ChromeDriver(options);
		
		logDriverInfo(driver);
		
		return new SeleniumDrivenBrowser(driver);
	}

	protected ChromeOptions createChromeOptions(IBrowserOptions browserOptions)
	{
		ChromeOptions options =
				  new ChromeOptions();
		options = options.addArguments("--no-sandbox", "--remote-allow-origins=*", "--disable-dev-shm-usage");
		options = setHeadless(browserOptions, options);
		options = setUnexpectedAlertBehaviour(browserOptions, options);
		return options;
	}

	private void logDriverInfo(ChromeDriver driver)
	{
		Capabilities capabilities = driver.getCapabilities();
		Map<String, String> chromeInfo = (Map<String, String>)capabilities.getCapability("chrome");
		LOGGER.info(String.format(
				  "Started Selenium driver: browserName[%s], browserVersion[%s], driverVersion[%s], dataDir[%s]",
				  capabilities.getBrowserName(),
				  capabilities.getBrowserVersion(),
				  chromeInfo.get("chromedriverVersion"),
				  chromeInfo.get("userDataDir")));
	}

	private void logSeleniumBuildInfo()
	{
		BuildInfo info = new BuildInfo();
		LOGGER.info(String.format(
				  "Selenium build info: %s",
				  info.toString()));
	}

	private ChromeOptions setUnexpectedAlertBehaviour(IBrowserOptions browserOptions, ChromeOptions options)
	{
		if(browserOptions.acceptUnexpectedAlerts())
		{
			options = (ChromeOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
		}
		else
		{
			options = (ChromeOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
		}
		return options;
	}

	private ChromeOptions setHeadless(IBrowserOptions browserOptions, ChromeOptions options)
	{
		if(browserOptions.isHeadless())
		{
			options = options.addArguments("--headless=new");
		}
		return options;
	}
}

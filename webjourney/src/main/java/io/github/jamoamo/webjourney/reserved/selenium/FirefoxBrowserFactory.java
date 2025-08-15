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
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.StandardBrowser;
import io.github.jamoamo.webjourney.api.web.ResolvedBrowserArguments;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Browser factory to create a firefox browser.
 *
 * @author James Amoore
 */
public final class FirefoxBrowserFactory implements IBrowserFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FirefoxBrowserFactory.class);
	private final IBrowserArgumentsProvider browserArgumentsProvider;
	private final AsyncConfiguration configuration;
	
	/**
	 * Creates a new FirefoxBrowserFactory with default configuration.
	 */
	public FirefoxBrowserFactory()
	{
		this(null, null);
	}
	
	/**
	 * Creates a new FirefoxBrowserFactory with provided configuration.
	 * 
	 * @param configuration The configuration for browser arguments.
	 * @param browserArgumentsProvider The provider for browser arguments. If null, a default provider will be created.
	 */
	public FirefoxBrowserFactory(AsyncConfiguration configuration, IBrowserArgumentsProvider browserArgumentsProvider)
	{
		this.configuration = configuration != null ? configuration : new AsyncConfiguration(java.util.List.of(), java.util.List.of());
		this.browserArgumentsProvider = browserArgumentsProvider != null ? browserArgumentsProvider : new DefaultBrowserArgumentsProvider(System::getenv, this.configuration);
	}
	
	/**
	 * Creates a new Firefox browser.
	 *
	 * @param browserOptions the options to use to create the browser
	 *
	 * @return a new Firefox browser instance.
	 */
	@Override
	public IBrowser createBrowser(IBrowserOptions browserOptions)
	{
		return createBrowser(browserOptions, null);
	}
	
	@Override
	public IBrowser createBrowser(IBrowserOptions browserOptions, IJourneyContext journeyContext)
	{
		FirefoxOptions options =
			createFirefoxOptions(browserOptions, journeyContext);
		
		logSeleniumBuildInfo();
		
		FirefoxDriver driver = new FirefoxDriver(options);
		
		logDriverInfo(driver);
		
		return new SeleniumDrivenBrowser(driver);
	}

	protected FirefoxOptions createFirefoxOptions(IBrowserOptions browserOptions)
	{
		return createFirefoxOptions(browserOptions, null);
	}
	
	protected FirefoxOptions createFirefoxOptions(IBrowserOptions browserOptions, IJourneyContext journeyContext)
	{
		FirefoxOptions options = new FirefoxOptions();
		
		// Apply basic settings
		options = setHeadless(browserOptions, options);
		options = setUnexpectedAlertBehaviour(browserOptions, options);
		
		// Apply browser arguments if feature is enabled and context is available
		if (journeyContext != null && this.configuration.isEnableExtraArgs())
		{
			try
			{
				ResolvedBrowserArguments resolved = this.browserArgumentsProvider.resolve(StandardBrowser.FIREFOX, journeyContext);
				if (!resolved.getArguments().isEmpty())
				{
					LOGGER.debug("Applying {} resolved Firefox arguments", resolved.getArguments().size());
					options.addArguments(resolved.getArguments());
				}
			}
			catch (Exception ex)
			{
				LOGGER.warn("Failed to resolve browser arguments, continuing with defaults: {}", ex.getMessage());
			}
		}
		
		return options;
	}

	private void logDriverInfo(FirefoxDriver driver)
	{
		Capabilities capabilities = driver.getCapabilities();
		LOGGER.info(String.format(
				  "Started Selenium driver: browserName[%s], browserVersion[%s]",
				  capabilities.getBrowserName(),
				  capabilities.getBrowserVersion()));
	}

	private void logSeleniumBuildInfo()
	{
		BuildInfo info = new BuildInfo();
		LOGGER.info(String.format(
				  "Selenium build info: %s",
				  info.toString()));
	}

	private FirefoxOptions setUnexpectedAlertBehaviour(IBrowserOptions browserOptions, FirefoxOptions options)
	{
		if(browserOptions.acceptUnexpectedAlerts())
		{
			options = (FirefoxOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.ACCEPT);
		}
		else
		{
			options = (FirefoxOptions) options.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.DISMISS);
		}
		return options;
	}

	private FirefoxOptions setHeadless(IBrowserOptions browserOptions, FirefoxOptions options)
	{
		if(browserOptions.isHeadless())
		{
			options.addArguments("--headless");
		}
		return options;
	}
}

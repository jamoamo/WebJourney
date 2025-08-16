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

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserFactory;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.web.IHubConfiguration;
import io.github.jamoamo.webjourney.api.web.IRemoteBrowserOptions;
import io.github.jamoamo.webjourney.api.web.HubConnectionException;
import io.github.jamoamo.webjourney.api.web.HubSessionException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for remote browser factories that connect to Selenium Hub.
 * Provides common functionality for creating RemoteWebDriver instances with
 * retry logic, connection management, and error handling.
 *
 * @param <T> the type of browser options (ChromeOptions, FirefoxOptions, etc.)
 * @author James Amoore
 */
public abstract class RemoteBrowserFactory<T extends Capabilities> implements IBrowserFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteBrowserFactory.class);
	
	protected final IHubConfiguration hubConfiguration;
	
	/**
	 * Creates a new remote browser factory with the specified hub configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 * @throws IllegalArgumentException if hubConfiguration is null
	 */
	protected RemoteBrowserFactory(IHubConfiguration hubConfiguration)
	{
		this.hubConfiguration = Objects.requireNonNull(hubConfiguration, "Hub configuration cannot be null");
	}
	
	@Override
	public final IBrowser createBrowser(IBrowserOptions options)
	{
		return createBrowser(options, null);
	}
	
	@Override
	public final IBrowser createBrowser(IBrowserOptions options, IJourneyContext context)
	{
		try
		{
			T browserOptions = createBrowserOptions(options, context);
			RemoteWebDriver driver = createRemoteDriver(browserOptions);
			return new SeleniumDrivenBrowser(driver);
		}
		catch (Exception e)
		{
			String hubUrl = hubConfiguration.getHubUrl();
			LOGGER.error("Failed to create remote browser on hub: {}", hubUrl, e);
			throw new HubConnectionException("Failed to create remote browser", e, hubUrl, 0);
		}
	}
	
	/**
	 * Creates browser-specific options for the given browser options and context.
	 * Subclasses must implement this method to create the appropriate options type
	 * (ChromeOptions, FirefoxOptions, etc.) and apply any browser arguments.
	 *
	 * @param options the browser options
	 * @param context the journey context (may be null)
	 * @return the browser-specific options
	 */
	protected abstract T createBrowserOptions(IBrowserOptions options, IJourneyContext context);
	
	/**
	 * Creates a RemoteWebDriver instance with the specified options.
	 * Includes retry logic and connection timeout handling.
	 *
	 * @param browserOptions the browser options to use
	 * @return a new RemoteWebDriver instance
	 * @throws Exception if driver creation fails after all retries
	 */
	private RemoteWebDriver createRemoteDriver(T browserOptions) throws Exception
	{
		if (!hubConfiguration.isEnabled())
		{
			throw new HubConnectionException("Hub connectivity is disabled in configuration");
		}
		
		String hubUrl = hubConfiguration.getHubUrl();
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			throw new HubConnectionException("Hub URL is not configured");
		}
		
		// Add custom capabilities
		if (!hubConfiguration.getCustomCapabilities().isEmpty())
		{
			MutableCapabilities customCaps = new MutableCapabilities(hubConfiguration.getCustomCapabilities());
			browserOptions.merge(customCaps);
		}
		
		URL url = new URL(hubUrl);
		int maxRetries = hubConfiguration.getMaxRetries();
		Duration retryDelay = hubConfiguration.getRetryDelay();
		
		Exception lastException = null;
		
		for (int attempt = 0; attempt <= maxRetries; attempt++)
		{
			try
			{
				LOGGER.debug("Attempting to create RemoteWebDriver (attempt {} of {}): {}", 
							attempt + 1, maxRetries + 1, hubUrl);
				
				RemoteWebDriver driver = new RemoteWebDriver(url, browserOptions);
				
				LOGGER.info("Successfully created RemoteWebDriver on hub: {} (sessionId: {})", 
						   hubUrl, driver.getSessionId());
				
				return driver;
			}
			catch (Exception e)
			{
				lastException = e;
				LOGGER.warn("Failed to create RemoteWebDriver (attempt {} of {}): {}", 
						   attempt + 1, maxRetries + 1, e.getMessage());
				
				if (attempt < maxRetries)
				{
					try
					{
						Thread.sleep(retryDelay.toMillis());
					}
					catch (InterruptedException ie)
					{
						Thread.currentThread().interrupt();
						throw new HubConnectionException("Interrupted while waiting to retry", ie, hubUrl, attempt);
					}
				}
			}
		}
		
		// All retries failed
		throw new HubSessionException("Failed to create browser session after " + (maxRetries + 1) + " attempts", 
									 lastException, hubUrl, null, maxRetries);
	}
	
	/**
	 * Gets the hub configuration used by this factory.
	 *
	 * @return the hub configuration
	 */
	public IHubConfiguration getHubConfiguration()
	{
		return hubConfiguration;
	}
	
	/**
	 * Validates hub connectivity by checking the hub status endpoint.
	 *
	 * @return true if the hub is available, false otherwise
	 */
	public boolean isHubAvailable()
	{
		return HubConnectionUtils.isHubAvailable(hubConfiguration.getHubUrl());
	}
}

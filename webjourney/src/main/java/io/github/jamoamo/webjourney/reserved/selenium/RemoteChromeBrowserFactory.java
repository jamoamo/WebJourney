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
import io.github.jamoamo.webjourney.api.web.IBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.web.IHubConfiguration;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserArgumentsProvider;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote browser factory for creating Chrome browsers on Selenium Hub.
 * This factory reuses all the existing Chrome browser configuration logic
 * from ChromeBrowserFactory but creates RemoteWebDriver instances instead
 * of local ChromeDriver instances.
 * 
 * @author James Amoore
 */
public final class RemoteChromeBrowserFactory extends RemoteBrowserFactory<ChromeOptions>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteChromeBrowserFactory.class);
	
	private final ChromeBrowserFactory localFactory;
	
	/**
	 * Creates a new remote Chrome browser factory with the specified hub configuration.
	 * Uses default browser arguments provider and configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 */
	public RemoteChromeBrowserFactory(IHubConfiguration hubConfiguration)
	{
		super(hubConfiguration);
		this.localFactory = new ChromeBrowserFactory();
	}
	
	/**
	 * Creates a new remote Chrome browser factory with custom configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 * @param browserArgumentsProvider the browser arguments provider (may be null for default)
	 */
	public RemoteChromeBrowserFactory(IHubConfiguration hubConfiguration, 
									 IBrowserArgumentsProvider browserArgumentsProvider)
	{
		super(hubConfiguration);
		
		// Create local factory with custom arguments provider if available
		// Use the default constructor - options creation will be delegated to this factory
		this.localFactory = new ChromeBrowserFactory();
	}
	
	@Override
	protected ChromeOptions createBrowserOptions(IBrowserOptions options, IJourneyContext context)
	{
		LOGGER.debug("Creating Chrome options for remote execution on hub: {}", 
					hubConfiguration.getHubUrl());
		
		// Delegate to the local Chrome factory to reuse all existing logic
		// This ensures we get all the same arguments, headless settings, alert behavior, etc.
		ChromeOptions chromeOptions = localFactory.createChromeOptions(options, context);
		
		LOGGER.debug("Created Chrome options for remote execution");
		
		return chromeOptions;
	}
	
	/**
	 * Gets the underlying local Chrome factory used for options creation.
	 *
	 * @return the local Chrome browser factory
	 */
	public ChromeBrowserFactory getLocalFactory()
	{
		return localFactory;
	}
}

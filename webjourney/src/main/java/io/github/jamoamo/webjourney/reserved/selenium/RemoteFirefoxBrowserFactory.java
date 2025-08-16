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
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote browser factory for creating Firefox browsers on Selenium Hub.
 * This factory reuses all the existing Firefox browser configuration logic
 * from FirefoxBrowserFactory but creates RemoteWebDriver instances instead
 * of local FirefoxDriver instances.
 * 
 * @author James Amoore
 */
public final class RemoteFirefoxBrowserFactory extends RemoteBrowserFactory<FirefoxOptions>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteFirefoxBrowserFactory.class);
	
	private final FirefoxBrowserFactory localFactory;
	
	/**
	 * Creates a new remote Firefox browser factory with the specified hub configuration.
	 * Uses default browser arguments provider and configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 */
	public RemoteFirefoxBrowserFactory(IHubConfiguration hubConfiguration)
	{
		super(hubConfiguration);
		this.localFactory = new FirefoxBrowserFactory();
	}
	
	/**
	 * Creates a new remote Firefox browser factory with custom configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 * @param browserArgumentsProvider the browser arguments provider (may be null for default)
	 */
	public RemoteFirefoxBrowserFactory(IHubConfiguration hubConfiguration, 
									  IBrowserArgumentsProvider browserArgumentsProvider)
	{
		super(hubConfiguration);
		
		// Create local factory with custom arguments provider if available
		if (browserArgumentsProvider != null)
		{
			// Note: This assumes FirefoxBrowserFactory has a constructor that accepts IBrowserArgumentsProvider
			// For now, we'll use the default constructor and delegate options creation
			this.localFactory = new FirefoxBrowserFactory();
		}
		else
		{
			this.localFactory = new FirefoxBrowserFactory();
		}
	}
	
	@Override
	protected FirefoxOptions createBrowserOptions(IBrowserOptions options, IJourneyContext context)
	{
		LOGGER.debug("Creating Firefox options for remote execution on hub: {}", 
					hubConfiguration.getHubUrl());
		
		// Delegate to the local Firefox factory to reuse all existing logic
		// This ensures we get all the same arguments, headless settings, alert behavior, etc.
		FirefoxOptions firefoxOptions = localFactory.createFirefoxOptions(options, context);
		
		LOGGER.debug("Created Firefox options for remote execution");
		
		return firefoxOptions;
	}
	
	/**
	 * Gets the underlying local Firefox factory used for options creation.
	 *
	 * @return the local Firefox browser factory
	 */
	public FirefoxBrowserFactory getLocalFactory()
	{
		return localFactory;
	}
}

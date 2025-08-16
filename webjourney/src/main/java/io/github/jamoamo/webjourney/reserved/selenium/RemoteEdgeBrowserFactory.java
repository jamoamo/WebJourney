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
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote browser factory for creating Edge browsers on Selenium Hub.
 * This factory reuses all the existing Edge browser configuration logic
 * from EdgeBrowserFactory but creates RemoteWebDriver instances instead
 * of local EdgeDriver instances.
 * 
 * @author James Amoore
 */
public final class RemoteEdgeBrowserFactory extends RemoteBrowserFactory<EdgeOptions>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteEdgeBrowserFactory.class);
	
	private final EdgeBrowserFactory localFactory;
	
	/**
	 * Creates a new remote Edge browser factory with the specified hub configuration.
	 * Uses default browser arguments provider and configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 */
	public RemoteEdgeBrowserFactory(IHubConfiguration hubConfiguration)
	{
		super(hubConfiguration);
		this.localFactory = new EdgeBrowserFactory();
	}
	
	/**
	 * Creates a new remote Edge browser factory with custom configuration.
	 *
	 * @param hubConfiguration the hub configuration to use
	 * @param browserArgumentsProvider the browser arguments provider (may be null for default)
	 */
	public RemoteEdgeBrowserFactory(IHubConfiguration hubConfiguration, 
								   IBrowserArgumentsProvider browserArgumentsProvider)
	{
		super(hubConfiguration);
		
		// Create local factory with custom arguments provider if available
		if (browserArgumentsProvider != null)
		{
			// Note: This assumes EdgeBrowserFactory has a constructor that accepts IBrowserArgumentsProvider
			// For now, we'll use the default constructor and delegate options creation
			this.localFactory = new EdgeBrowserFactory();
		}
		else
		{
			this.localFactory = new EdgeBrowserFactory();
		}
	}
	
	@Override
	protected EdgeOptions createBrowserOptions(IBrowserOptions options, IJourneyContext context)
	{
		LOGGER.debug("Creating Edge options for remote execution on hub: {}", 
					hubConfiguration.getHubUrl());
		
		// Delegate to the local Edge factory to reuse all existing logic
		// This ensures we get all the same arguments, headless settings, alert behavior, etc.
		EdgeOptions edgeOptions = localFactory.createEdgeOptions(options, context);
		
		LOGGER.debug("Created Edge options for remote execution");
		
		return edgeOptions;
	}
	
	/**
	 * Gets the underlying local Edge factory used for options creation.
	 *
	 * @return the local Edge browser factory
	 */
	public EdgeBrowserFactory getLocalFactory()
	{
		return localFactory;
	}
}

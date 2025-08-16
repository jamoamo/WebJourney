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
package io.github.jamoamo.webjourney.api.web;

import java.time.Duration;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Browser strategy that intelligently chooses between remote hub execution and local fallback.
 * This strategy attempts to create browsers on a remote hub first, and falls back to
 * local execution if the hub is unavailable or browser creation fails.
 * 
 * @author James Amoore
 */
public class HubAwareBrowserStrategy implements IPreferredBrowserStrategy
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HubAwareBrowserStrategy.class);
	
	private final IBrowserFactory remoteFactory;
	private final IBrowserFactory localFactory;
	private final IGridHealthMonitor healthMonitor;
	private final boolean enableFallback;
	private final boolean enableHealthMonitoring;
	
	/**
	 * Creates a new hub-aware browser strategy.
	 *
	 * @param remoteFactory the remote browser factory to use for hub execution
	 * @param localFactory the local browser factory to use for fallback
	 */
	public HubAwareBrowserStrategy(IBrowserFactory remoteFactory, IBrowserFactory localFactory)
	{
		this(remoteFactory, localFactory, true, true);
	}
	
	/**
	 * Creates a new hub-aware browser strategy with configuration options.
	 *
	 * @param remoteFactory the remote browser factory to use for hub execution
	 * @param localFactory the local browser factory to use for fallback
	 * @param enableFallback whether to enable fallback to local execution
	 * @param enableHealthMonitoring whether to enable hub health monitoring
	 */
	public HubAwareBrowserStrategy(IBrowserFactory remoteFactory, IBrowserFactory localFactory,
								  boolean enableFallback, boolean enableHealthMonitoring)
	{
		this.remoteFactory = Objects.requireNonNull(remoteFactory, "Remote factory cannot be null");
		this.localFactory = enableFallback ? 
			Objects.requireNonNull(localFactory, "Local factory cannot be null when fallback is enabled") : 
			localFactory;
		this.enableFallback = enableFallback;
		this.enableHealthMonitoring = enableHealthMonitoring;
		this.healthMonitor = enableHealthMonitoring ? new GridHealthMonitor() : null;
		
		// Start health monitoring if enabled
		if (enableHealthMonitoring && healthMonitor != null)
		{
			// Extract hub URL from remote factory if possible
			if (remoteFactory instanceof io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory)
			{
				io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory<?> rbf = 
					(io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory<?>) remoteFactory;
				String hubUrl = rbf.getHubConfiguration().getHubUrl();
				if (hubUrl != null)
				{
					healthMonitor.addHub(hubUrl);
					healthMonitor.startMonitoring(Duration.ofSeconds(30));
				}
			}
		}
	}
	
	@Override
	public IBrowser getPreferredBrowser(IBrowserOptions options)
	{
		// Check if remote execution is explicitly configured
		boolean useRemote = shouldUseRemote(options);
		
		if (useRemote)
		{
			try
			{
				LOGGER.debug("Attempting to create browser on remote hub");
				IBrowser remoteBrowser = remoteFactory.createBrowser(options);
				LOGGER.info("Successfully created browser on remote hub");
				return remoteBrowser;
			}
			catch (Exception e)
			{
				LOGGER.warn("Failed to create browser on remote hub: {}", e.getMessage());
				
				if (enableFallback && localFactory != null)
				{
					LOGGER.info("Falling back to local browser execution");
					return createLocalBrowser(options);
				}
				else
				{
					throw new RemoteBrowserException("Remote browser creation failed and fallback is disabled", e);
				}
			}
		}
		else
		{
			// Use local execution directly
			return createLocalBrowser(options);
		}
	}
	
	/**
	 * Determines whether to use remote execution based on options and hub availability.
	 */
	private boolean shouldUseRemote(IBrowserOptions options)
	{
		// If options explicitly specify remote execution
		if (options instanceof IRemoteBrowserOptions)
		{
			IRemoteBrowserOptions remoteOptions = (IRemoteBrowserOptions) options;
			if (remoteOptions.isRemoteExecution())
			{
				return isHubAvailable(remoteOptions.getHubConfiguration());
			}
		}
		
		// Check if remote factory can be used (has hub configuration)
		if (remoteFactory instanceof io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory)
		{
			io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory<?> rbf = 
				(io.github.jamoamo.webjourney.reserved.selenium.RemoteBrowserFactory<?>) remoteFactory;
			return isHubAvailable(rbf.getHubConfiguration());
		}
		
		// Default to local execution if we can't determine remote availability
		return false;
	}
	
	/**
	 * Checks if a hub is available based on configuration and health monitoring.
	 */
	private boolean isHubAvailable(IHubConfiguration hubConfig)
	{
		if (hubConfig == null || !hubConfig.isEnabled())
		{
			return false;
		}
		
		String hubUrl = hubConfig.getHubUrl();
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			return false;
		}
		
		// Use health monitor if available and monitoring is enabled
		if (enableHealthMonitoring && healthMonitor != null)
		{
			return healthMonitor.isHubAvailable(hubUrl);
		}
		
		// Fallback to direct availability check
		return io.github.jamoamo.webjourney.reserved.selenium.HubConnectionUtils.isHubAvailable(hubUrl);
	}
	
	/**
	 * Creates a browser using the local factory.
	 */
	private IBrowser createLocalBrowser(IBrowserOptions options)
	{
		if (localFactory == null)
		{
			throw new IllegalStateException("Local factory is not configured");
		}
		
		// Extract local options if wrapped in remote options
		IBrowserOptions localOptions = options;
		if (options instanceof IRemoteBrowserOptions)
		{
			IRemoteBrowserOptions remoteOptions = (IRemoteBrowserOptions) options;
			if (remoteOptions instanceof RemoteBrowserOptionsAdapter)
			{
				RemoteBrowserOptionsAdapter adapter = (RemoteBrowserOptionsAdapter) remoteOptions;
				localOptions = adapter.getLocalOptions();
			}
		}
		
		return localFactory.createBrowser(localOptions);
	}
	
	/**
	 * Gets the remote browser factory.
	 *
	 * @return the remote browser factory
	 */
	public IBrowserFactory getRemoteFactory()
	{
		return remoteFactory;
	}
	
	/**
	 * Gets the local browser factory.
	 *
	 * @return the local browser factory, or null if fallback is disabled
	 */
	public IBrowserFactory getLocalFactory()
	{
		return localFactory;
	}
	
	/**
	 * Gets the health monitor if enabled.
	 *
	 * @return the health monitor, or null if health monitoring is disabled
	 */
	public IGridHealthMonitor getHealthMonitor()
	{
		return healthMonitor;
	}
	
	/**
	 * Determines if fallback to local execution is enabled.
	 *
	 * @return true if fallback is enabled, false otherwise
	 */
	public boolean isFallbackEnabled()
	{
		return enableFallback;
	}
	
	/**
	 * Determines if health monitoring is enabled.
	 *
	 * @return true if health monitoring is enabled, false otherwise
	 */
	public boolean isHealthMonitoringEnabled()
	{
		return enableHealthMonitoring;
	}
	
	/**
	 * Shuts down the strategy and any associated resources.
	 * This should be called when the strategy is no longer needed.
	 */
	public void shutdown()
	{
		if (healthMonitor != null && healthMonitor.isMonitoring())
		{
			healthMonitor.stopMonitoring();
		}
	}
}

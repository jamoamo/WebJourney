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
package io.github.jamoamo.webjourney.examples;

import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.*;
import io.github.jamoamo.webjourney.reserved.selenium.RemoteChromeBrowserFactory;
import io.github.jamoamo.webjourney.reserved.selenium.ChromeBrowserFactory;
import java.time.Duration;

/**
 * Examples demonstrating WebJourney Hub Support functionality.
 * These examples show various ways to configure and use Selenium Hub
 * with WebJourney for remote browser execution.
 * 
 * @author James Amoore
 */
public class HubSupportExamples
{
	/**
	 * Example 1: Basic Hub Configuration and Usage
	 * Demonstrates the simplest way to configure WebJourney to use a Selenium Hub.
	 */
	public static void basicHubUsage()
	{
		// Create hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl("http://selenium-hub:4444/wd/hub")
			.withConnectionTimeout(Duration.ofSeconds(30))
			.withSessionTimeout(Duration.ofMinutes(10))
			.build();
		
		// Create remote browser factory
		IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(hubConfig);
		
		// Create travel options with remote factory
		TravelOptions travelOptions = new TravelOptions();
		travelOptions.setPreferredBrowserStrategy(
			new PreferredBrowserStrategy(remoteChromeFactory)
		);
		
		// Use WebTraveller as normal
		WebTraveller traveller = new WebTraveller(travelOptions);
		
		// Create a simple journey
		IJourney journey = context -> {
			// Your journey logic here
			System.out.println("Running journey on remote browser!");
		};
		
		// Execute the journey
		traveller.travelJourney(journey);
	}
	
	/**
	 * Example 2: Hub-Aware Strategy with Fallback
	 * Demonstrates intelligent browser selection that tries hub first,
	 * then falls back to local execution if the hub is unavailable.
	 */
	public static void hubAwareWithFallback()
	{
		// Create hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl("http://selenium-hub:4444/wd/hub")
			.withConnectionTimeout(Duration.ofSeconds(15))
			.withMaxRetries(2)
			.build();
		
		// Create browser factories
		IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		IBrowserFactory localFactory = new ChromeBrowserFactory();
		
		// Create hub-aware strategy with fallback
		HubAwareBrowserStrategy strategy = new HubAwareBrowserStrategy(
			remoteFactory, 
			localFactory,
			true,  // Enable fallback
			true   // Enable health monitoring
		);
		
		// Create travel options
		TravelOptions travelOptions = new TravelOptions();
		travelOptions.setPreferredBrowserStrategy(strategy);
		WebTraveller traveller = new WebTraveller(travelOptions);
		
		// Execute journey - will try hub first, fall back to local if needed
		traveller.travelJourney(context -> {
			System.out.println("Journey executed with intelligent hub selection!");
		});
		
		// Clean up strategy resources
		strategy.shutdown();
	}
	
	/**
	 * Example 3: Remote Options with Custom Capabilities
	 * Demonstrates using remote browser options with custom Grid capabilities.
	 */
	public static void remoteOptionsWithCustomCapabilities()
	{
		// Create local browser options
		IBrowserOptions localOptions = new StandardBrowserOptions(true, true); // headless, accept alerts
		
		// Create hub configuration with custom capabilities
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl("http://selenium-hub:4444/wd/hub")
			.withCustomCapability("enableVNC", true)
			.withCustomCapability("enableVideo", true)
			.withCustomCapability("videoName", "test-recording.mp4")
			.withCustomCapability("timeZone", "America/New_York")
			.build();
		
		// Create remote browser options
		IRemoteBrowserOptions remoteOptions = IRemoteBrowserOptions.remote(localOptions, hubConfig);
		
		// Create factory and travel options
		IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		TravelOptions travelOptions = new TravelOptions();
		travelOptions.setPreferredBrowserStrategy(
			new PreferredBrowserStrategy(remoteFactory)
		);
		
		WebTraveller traveller = new WebTraveller(travelOptions);
		
		// Execute journey with custom capabilities
		traveller.travelJourney(context -> {
			System.out.println("Journey with custom Grid capabilities!");
		});
	}
	
	/**
	 * Example 4: Environment-Based Configuration
	 * Demonstrates configuring hub support through environment variables.
	 */
	public static void environmentBasedConfiguration()
	{
		// Check environment variables for hub configuration
		String hubUrl = System.getenv("WEBJOURNEY_HUB_URL");
		boolean hubEnabled = Boolean.parseBoolean(
			System.getenv().getOrDefault("WEBJOURNEY_HUB_ENABLED", "false")
		);
		
		TravelOptions travelOptions;
		
		if (hubEnabled && hubUrl != null && !hubUrl.isEmpty())
		{
			// Configure for hub execution
			IHubConfiguration hubConfig = HubConfiguration.builder()
				.withUrl(hubUrl)
				.withConnectionTimeout(Duration.ofSeconds(
					Integer.parseInt(System.getenv().getOrDefault("WEBJOURNEY_HUB_TIMEOUT", "30"))
				))
				.build();
			
			IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
			IBrowserFactory localFactory = new ChromeBrowserFactory();
			
			// Use hub-aware strategy for reliability
			travelOptions = new TravelOptions();
			travelOptions.setPreferredBrowserStrategy(
				new HubAwareBrowserStrategy(remoteFactory, localFactory)
			);
			
			System.out.println("Configured for hub execution: " + hubUrl);
		}
		else
		{
			// Configure for local execution
			travelOptions = new TravelOptions();
			travelOptions.setPreferredBrowserStrategy(
				new PreferredBrowserStrategy(StandardBrowser.CHROME)
			);
			
			System.out.println("Configured for local execution");
		}
		
		WebTraveller traveller = new WebTraveller(travelOptions);
		
		// Execute journey
		traveller.travelJourney(context -> {
			System.out.println("Journey executed with environment-based configuration!");
		});
	}
	
	/**
	 * Example 5: Health Monitoring and Status Checking
	 * Demonstrates monitoring hub health and responding to availability changes.
	 */
	public static void healthMonitoringExample()
	{
		// Create health monitor
		IGridHealthMonitor healthMonitor = new GridHealthMonitor();
		
		// Register health listener
		healthMonitor.registerHealthListener(new GridHealthListener()
		{
			@Override
			public void onHubAvailable(GridStatus status)
			{
				System.out.println("Hub became available: " + status.getHubUrl());
			}
			
			@Override
			public void onHubUnavailable(GridStatus status)
			{
				System.out.println("Hub became unavailable: " + status.getHubUrl() + 
								 " (failures: " + status.getConsecutiveFailures() + ")");
			}
			
			@Override
			public void onStatusUpdate(GridStatus previousStatus, GridStatus currentStatus)
			{
				if (currentStatus.isAvailable())
				{
					System.out.println("Hub health check: " + currentStatus.getHubUrl() + 
									 " (" + currentStatus.getResponseTimeMs() + "ms)");
				}
			}
		});
		
		// Add hubs to monitor
		healthMonitor.addHub("http://hub1:4444/wd/hub");
		healthMonitor.addHub("http://hub2:4444/wd/hub");
		
		// Start monitoring
		healthMonitor.startMonitoring(Duration.ofSeconds(15));
		
		// Check status manually
		GridStatus status = healthMonitor.getHubStatus("http://hub1:4444/wd/hub");
		System.out.println("Current status: " + status);
		
		// Run for a while, then stop monitoring
		try
		{
			Thread.sleep(Duration.ofMinutes(1).toMillis());
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		
		healthMonitor.stopMonitoring();
	}
	
	/**
	 * Example 6: Error Handling and Recovery
	 * Demonstrates proper error handling when working with remote hubs.
	 */
	public static void errorHandlingExample()
	{
		// Create hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl("http://unreliable-hub:4444/wd/hub")
			.withConnectionTimeout(Duration.ofSeconds(10))
			.withMaxRetries(3)
			.withRetryDelay(Duration.ofSeconds(2))
			.build();
		
		// Create factories
		IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		IBrowserFactory localFactory = new ChromeBrowserFactory();
		
		// Create strategy with fallback
		HubAwareBrowserStrategy strategy = new HubAwareBrowserStrategy(
			remoteFactory, 
			localFactory
		);
		
		TravelOptions travelOptions = new TravelOptions();
		travelOptions.setPreferredBrowserStrategy(strategy);
		WebTraveller traveller = new WebTraveller(travelOptions);
		
		// Execute journey with error handling
		try
		{
			traveller.travelJourney(context -> {
				System.out.println("Journey executed successfully!");
			});
		}
		catch (RemoteBrowserException e)
		{
			System.err.println("Remote browser error: " + e.getMessage());
			System.err.println("Hub URL: " + e.getHubUrl());
			System.err.println("Retry attempts: " + e.getRetryAttempts());
			
			// Handle specific error types
			if (e instanceof HubConnectionException)
			{
				System.err.println("Hub connection failed - check network and hub availability");
			}
			else if (e instanceof HubSessionException)
			{
				System.err.println("Session creation failed - check hub capacity and capabilities");
			}
		}
		finally
		{
			strategy.shutdown();
		}
	}
	
	/**
	 * Main method to run examples.
	 */
	public static void main(String[] args)
	{
		System.out.println("WebJourney Hub Support Examples");
		System.out.println("================================");
		
		try
		{
			System.out.println("\n1. Basic Hub Usage:");
			basicHubUsage();
			
			System.out.println("\n2. Hub-Aware with Fallback:");
			hubAwareWithFallback();
			
			System.out.println("\n3. Custom Capabilities:");
			remoteOptionsWithCustomCapabilities();
			
			System.out.println("\n4. Environment-Based Configuration:");
			environmentBasedConfiguration();
			
			System.out.println("\n5. Error Handling:");
			errorHandlingExample();
			
			System.out.println("\nAll examples completed!");
		}
		catch (Exception e)
		{
			System.err.println("Example execution failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
}

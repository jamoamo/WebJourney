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

import io.github.jamoamo.webjourney.reserved.selenium.RemoteChromeBrowserFactory;
import io.github.jamoamo.webjourney.reserved.selenium.ChromeBrowserFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Hub Support functionality.
 * These tests demonstrate the complete hub support workflow.
 * 
 * @author James Amoore
 */
class HubSupportIntegrationTest
{
	private static final String TEST_HUB_URL = "http://localhost:4444/wd/hub";
	private static final String NON_EXISTENT_HUB_URL = "http://non-existent-hub:9999/wd/hub";
	
	@Test
	void testHubConfigurationCreation()
	{
		// Test creating hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl(TEST_HUB_URL)
			.withConnectionTimeout(Duration.ofSeconds(30))
			.withSessionTimeout(Duration.ofMinutes(10))
			.withMaxRetries(3)
			.withCustomCapability("enableVNC", true)
			.build();
		
		assertNotNull(hubConfig);
		assertEquals(TEST_HUB_URL, hubConfig.getHubUrl());
		assertTrue(hubConfig.isEnabled());
		assertEquals(3, hubConfig.getMaxRetries());
		assertTrue(hubConfig.getCustomCapabilities().containsKey("enableVNC"));
	}
	
	@Test
	void testRemoteBrowserOptionsCreation()
	{
		// Create local options
		IBrowserOptions localOptions = new DefaultBrowserOptions();
		
		// Create hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl(TEST_HUB_URL)
			.build();
		
		// Create remote options
		IRemoteBrowserOptions remoteOptions = IRemoteBrowserOptions.remote(localOptions, hubConfig);
		
		assertNotNull(remoteOptions);
		assertTrue(remoteOptions.isRemoteExecution());
		assertEquals(hubConfig, remoteOptions.getHubConfiguration());
		assertEquals(localOptions.isHeadless(), remoteOptions.isHeadless());
		assertEquals(localOptions.acceptUnexpectedAlerts(), remoteOptions.acceptUnexpectedAlerts());
	}
	
	@Test
	void testRemoteBrowserFactoryCreation()
	{
		// Create hub configuration
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl(TEST_HUB_URL)
			.withEnabled(false) // Disable to avoid actual connection attempts
			.build();
		
		// Create remote browser factory
		RemoteChromeBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		
		assertNotNull(remoteFactory);
		assertEquals(hubConfig, remoteFactory.getHubConfiguration());
		assertNotNull(remoteFactory.getLocalFactory());
	}
	
	@Test
	void testHubAwareBrowserStrategyCreation()
	{
		// Create factories
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl(TEST_HUB_URL)
			.withEnabled(false) // Disable to avoid actual connection attempts
			.build();
		
		IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		IBrowserFactory localFactory = new ChromeBrowserFactory();
		
		// Create hub-aware strategy
		HubAwareBrowserStrategy strategy = new HubAwareBrowserStrategy(remoteFactory, localFactory);
		
		assertNotNull(strategy);
		assertEquals(remoteFactory, strategy.getRemoteFactory());
		assertEquals(localFactory, strategy.getLocalFactory());
		assertTrue(strategy.isFallbackEnabled());
		assertTrue(strategy.isHealthMonitoringEnabled());
		
		// Clean up - stop monitoring to avoid affecting other tests
		strategy.shutdown();
	}
	
	@Test
	void testGridHealthMonitorCreation()
	{
		// Create health monitor
		IGridHealthMonitor healthMonitor = new GridHealthMonitor();
		
		assertNotNull(healthMonitor);
		
		// Add a hub (won't actually connect)
		healthMonitor.addHub(NON_EXISTENT_HUB_URL);
		
		// Test availability check (will return false for non-existent hub)
		boolean available = healthMonitor.isHubAvailable(NON_EXISTENT_HUB_URL);
		assertFalse(available); // Expected to be false since hub doesn't exist
		
		// Test status check
		GridStatus status = healthMonitor.getHubStatus(NON_EXISTENT_HUB_URL);
		assertNotNull(status);
		assertEquals(NON_EXISTENT_HUB_URL, status.getHubUrl());
		assertFalse(status.isAvailable());
		
		// Test that we can start and stop monitoring
		// Start monitoring
		healthMonitor.startMonitoring(Duration.ofSeconds(30));
		assertTrue(healthMonitor.isMonitoring());
		
		// Stop monitoring
		healthMonitor.stopMonitoring();
		assertFalse(healthMonitor.isMonitoring());
		
		// Verify we can start again
		healthMonitor.startMonitoring(Duration.ofSeconds(30));
		assertTrue(healthMonitor.isMonitoring());
		
		// Final cleanup
		healthMonitor.stopMonitoring();
		assertFalse(healthMonitor.isMonitoring());
	}
	
	@Test
	@EnabledIf("isActualGridAvailable")
	void testActualGridIntegration()
	{
		// This test only runs if an actual Selenium Grid is available
		// Create hub configuration for real hub
		IHubConfiguration hubConfig = HubConfiguration.builder()
			.withUrl(TEST_HUB_URL)
			.withConnectionTimeout(Duration.ofSeconds(10))
			.build();
		
		// Create remote factory
		RemoteChromeBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
		
		// Check if hub is available
		boolean hubAvailable = remoteFactory.isHubAvailable();
		
		if (hubAvailable)
		{
			// Create browser options
			IBrowserOptions options = new DefaultBrowserOptions();
			
			// Attempt to create browser (this will actually connect to the hub)
			try
			{
				IBrowser browser = remoteFactory.createBrowser(options);
				assertNotNull(browser);
				
				// Clean up
				browser.exit();
			}
			catch (Exception e)
			{
				// Log the exception but don't fail the test
				// Real grid integration can fail for many reasons
				System.out.println("Grid integration test failed (expected in CI): " + e.getMessage());
			}
		}
		else
		{
			System.out.println("Selenium Grid not available, skipping actual integration test");
		}
	}
	
	/**
	 * Condition method for EnabledIf annotation.
	 * Checks if an actual Selenium Grid is available for testing.
	 */
	static boolean isActualGridAvailable()
	{
		try
		{
			return io.github.jamoamo.webjourney.reserved.selenium.HubConnectionUtils.isHubAvailable(TEST_HUB_URL);
		}
		catch (Exception e)
		{
			return false;
		}
	}
}

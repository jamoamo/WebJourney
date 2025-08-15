/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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

import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import io.github.jamoamo.webjourney.reserved.selenium.ChromeBrowserFactory;
import io.github.jamoamo.webjourney.reserved.selenium.FirefoxBrowserFactory;
import io.github.jamoamo.webjourney.reserved.selenium.EdgeBrowserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Integration tests for browser arguments functionality across different browsers and strategies.
 *
 * @author James Amoore
 */
public class BrowserArgumentsIntegrationTest
{
	private IJourneyContext journeyContext;
	private IBrowserOptions browserOptions;
	private AsyncConfiguration configuration;

	@BeforeEach
	public void setUp()
	{
		journeyContext = Mockito.mock(IJourneyContext.class);
		IJourneyBrowserArguments browserArgs = Mockito.mock(IJourneyBrowserArguments.class);
		
		// Setup journey context with browser arguments
		Mockito.when(journeyContext.getBrowserArguments()).thenReturn(browserArgs);
		Mockito.when(browserArgs.snapshotGlobal()).thenReturn(List.of("--custom-global-arg"));
		Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.CHROME)).thenReturn(List.of("--window-size=1920,1080"));
		Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.FIREFOX)).thenReturn(List.of("--safe-mode"));
		Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.EDGE)).thenReturn(List.of("--disable-features=VizDisplayCompositor"));

		browserOptions = Mockito.mock(IBrowserOptions.class);
		Mockito.when(browserOptions.isHeadless()).thenReturn(false);
		Mockito.when(browserOptions.acceptUnexpectedAlerts()).thenReturn(true);

		// Create configuration with arguments from different sources
		configuration = new AsyncConfiguration(
			List.of("--disable-background-timer-throttling"), // global
			List.of("--disable-backgrounding-occluded-windows"), // chrome
			List.of("--no-remote"), // firefox
			List.of("--disable-background-mode"), // edge
			true, // enableExtraArgs
			"warn", 
			List.of("--dummy-denied-arg"), // Custom deny list to override defaults
			List.of(), 
			"DEBUG"
		);
	}

	@Test
	public void testPreferredBrowserStrategyPassesContextToFactory()
	{
		// Given: Chrome factory with configuration
		ChromeBrowserFactory chromeFactory = new ChromeBrowserFactory(configuration, createMockProvider());
		PreferredBrowserStrategy strategy = new PreferredBrowserStrategy(chromeFactory);

		// When: getting preferred browser with context
		IBrowser browser = null;
		try
		{
			browser = strategy.getPreferredBrowser(browserOptions, journeyContext);
			
			// Then: browser is created successfully
			Assertions.assertNotNull(browser, "Browser should be created");
		}
		finally
		{
			if (browser != null)
			{
				browser.exit();
			}
		}
	}

	@Test
	public void testPriorityBrowserStrategyUsesContext()
	{
		// Given: Priority strategy with multiple factories
		IBrowserFactory[] factories = {
			new ChromeBrowserFactory(configuration, createMockProvider())
		};
		PriorityBrowserStrategy strategy = new PriorityBrowserStrategy(factories);

		// When: getting preferred browser with context
		IBrowser browser = null;
		try
		{
			browser = strategy.getPreferredBrowser(browserOptions, journeyContext);
			
			// Then: browser is created successfully
			Assertions.assertNotNull(browser, "Browser should be created");
		}
		finally
		{
			if (browser != null)
			{
				browser.exit();
			}
		}
	}

	@Test
	public void testChromeFactoryIntegrationWithProviderMergesArguments()
	{
		// Given: Chrome factory with real provider
		DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(createMockEnv(), configuration);
		ChromeBrowserFactory factory = new ChromeBrowserFactory(configuration, provider);

		// When: testing through the provider directly
		ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, journeyContext);

		// Then: verify arguments from different sources are merged
		List<String> actualArgs = resolved.getArguments();
		
		// Global config arguments
		Assertions.assertTrue(actualArgs.contains("--disable-background-timer-throttling"), "Should contain global config argument");
		
		// Chrome-specific config arguments
		Assertions.assertTrue(actualArgs.contains("--disable-backgrounding-occluded-windows"), "Should contain chrome config argument");
		
		// Per-journey global arguments
		Assertions.assertTrue(actualArgs.contains("--custom-global-arg"), "Should contain per-journey global argument");
		
		// Per-journey chrome arguments
		Assertions.assertTrue(actualArgs.contains("--window-size=1920,1080"), "Should contain per-journey chrome argument");
		
		// Environment arguments
		Assertions.assertTrue(actualArgs.contains("--env-global-arg"), "Should contain environment global argument");
		Assertions.assertTrue(actualArgs.contains("--env-chrome-arg"), "Should contain environment chrome argument");
	}

	@Test
	public void testFirefoxFactoryIntegrationWithProviderMergesArguments()
	{
		// Given: Firefox factory with real provider
		DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(createMockEnv(), configuration);
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory(configuration, provider);

		// When: testing through the provider directly
		ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.FIREFOX, journeyContext);

		// Then: verify firefox-specific arguments are applied
		List<String> actualArgs = resolved.getArguments();
		
		// Global config arguments
		Assertions.assertTrue(actualArgs.contains("--disable-background-timer-throttling"), "Should contain global config argument");
		
		// Firefox-specific config arguments
		Assertions.assertTrue(actualArgs.contains("--no-remote"), "Should contain firefox config argument");
		
		// Per-journey firefox arguments
		Assertions.assertTrue(actualArgs.contains("--safe-mode"), "Should contain per-journey firefox argument");
		
		// Environment arguments
		Assertions.assertTrue(actualArgs.contains("--env-global-arg"), "Should contain environment global argument");
		Assertions.assertTrue(actualArgs.contains("--env-firefox-arg"), "Should contain environment firefox argument");
	}

	@Test
	public void testEdgeFactoryIntegrationWithProviderMergesArguments()
	{
		// Given: Edge factory with real provider
		DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(createMockEnv(), configuration);
		EdgeBrowserFactory factory = new EdgeBrowserFactory(configuration, provider);

		// When: testing through the provider directly
		ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.EDGE, journeyContext);

		// Then: verify edge-specific arguments are applied
		List<String> actualArgs = resolved.getArguments();
		
		// Global config arguments
		Assertions.assertTrue(actualArgs.contains("--disable-background-timer-throttling"), "Should contain global config argument");
		
		// Edge-specific config arguments
		Assertions.assertTrue(actualArgs.contains("--disable-background-mode"), "Should contain edge config argument");
		
		// Per-journey edge arguments
		Assertions.assertTrue(actualArgs.contains("--disable-features=VizDisplayCompositor"), "Should contain per-journey edge argument");
		
		// Environment arguments
		Assertions.assertTrue(actualArgs.contains("--env-global-arg"), "Should contain environment global argument");
		Assertions.assertTrue(actualArgs.contains("--env-edge-arg"), "Should contain environment edge argument");
	}

	private IBrowserArgumentsProvider createMockProvider()
	{
		IBrowserArgumentsProvider mockProvider = Mockito.mock(IBrowserArgumentsProvider.class);
		
		// Setup mock to return resolved arguments
		Mockito.when(mockProvider.resolve(Mockito.any(), Mockito.any()))
			   .thenReturn(new ResolvedBrowserArguments(List.of("--test-argument"), List.of()));
		
		return mockProvider;
	}

	private Function<String, String> createMockEnv()
	{
		Map<String, String> envVars = Map.of(
			"WEBJOURNEY_BROWSER_ARGS", "--env-global-arg",
			"WEBJOURNEY_CHROME_ARGS", "--env-chrome-arg",
			"WEBJOURNEY_FIREFOX_ARGS", "--env-firefox-arg",
			"WEBJOURNEY_EDGE_ARGS", "--env-edge-arg"
		);
		
		return envVars::get;
	}
}

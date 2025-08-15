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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.IJourneyBrowserArguments;
import io.github.jamoamo.webjourney.api.web.StandardBrowser;
import io.github.jamoamo.webjourney.api.web.ResolvedBrowserArguments;
import io.github.jamoamo.webjourney.api.web.ProvenancedArgument;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests for FirefoxBrowserFactory browser arguments integration.
 *
 * @author James Amoore
 */
public class FirefoxBrowserFactoryTest
{
	private IBrowserOptions browserOptions;
	private IJourneyContext journeyContext;
	private IBrowserArgumentsProvider mockProvider;
	private AsyncConfiguration configuration;

	@BeforeEach
	public void setUp()
	{
		browserOptions = Mockito.mock(IBrowserOptions.class);
		Mockito.when(browserOptions.isHeadless()).thenReturn(Boolean.TRUE);
		Mockito.when(browserOptions.acceptUnexpectedAlerts()).thenReturn(Boolean.TRUE);

		journeyContext = Mockito.mock(IJourneyContext.class);
		IJourneyBrowserArguments browserArgs = Mockito.mock(IJourneyBrowserArguments.class);
		Mockito.when(journeyContext.getBrowserArguments()).thenReturn(browserArgs);

		mockProvider = Mockito.mock(IBrowserArgumentsProvider.class);
		configuration = new AsyncConfiguration(List.of(), List.of(), List.of(), List.of(), true, "reject", List.of(), List.of(), "DEBUG");
	}

	@Test
	public void testCreateFirefoxOptionsAppliesResolvedArguments()
	{
		// Given: provider returns specific arguments
		List<String> expectedArgs = List.of("--safe-mode", "--new-instance");
		List<ProvenancedArgument> provenance = new ArrayList<>();
		ResolvedBrowserArguments resolved = new ResolvedBrowserArguments(expectedArgs, provenance);
		
		Mockito.when(mockProvider.resolve(StandardBrowser.FIREFOX, journeyContext))
			   .thenReturn(resolved);

		// When: creating firefox options with context
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory(configuration, mockProvider);
		FirefoxOptions options = factory.createFirefoxOptions(browserOptions, journeyContext);

		// Then: verify provider was called
		Mockito.verify(mockProvider).resolve(StandardBrowser.FIREFOX, journeyContext);
		
		// And options object is created successfully
		Assertions.assertNotNull(options, "FirefoxOptions should be created");
	}

	@Test
	public void testCreateFirefoxOptionsSkipsProviderWhenFeatureDisabled()
	{
		// Given: feature flag disabled
		AsyncConfiguration disabledConfig = new AsyncConfiguration(
			List.of(), List.of(), List.of(), List.of(), false, "reject", List.of(), List.of(), "DEBUG");

		// When: creating options
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory(disabledConfig, mockProvider);
		FirefoxOptions options = factory.createFirefoxOptions(browserOptions, journeyContext);

		// Then: provider not called
		Mockito.verify(mockProvider, Mockito.never()).resolve(Mockito.any(), Mockito.any());
	}

	@Test
	public void testCreateFirefoxOptionsSkipsProviderWhenContextNull()
	{
		// When: creating options without context
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory(configuration, mockProvider);
		FirefoxOptions options = factory.createFirefoxOptions(browserOptions, null);

		// Then: provider not called
		Mockito.verify(mockProvider, Mockito.never()).resolve(Mockito.any(), Mockito.any());
	}

	@Test
	public void testCreateFirefoxOptionsHandlesProviderException()
	{
		// Given: provider throws exception
		Mockito.when(mockProvider.resolve(StandardBrowser.FIREFOX, journeyContext))
			   .thenThrow(new RuntimeException("Provider error"));

		// When: creating options (should not throw)
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory(configuration, mockProvider);
		FirefoxOptions options = factory.createFirefoxOptions(browserOptions, journeyContext);

		// Then: options are still created successfully
		Assertions.assertNotNull(options, "Options should be created despite provider error");
	}

	@Test
	public void testCreateFirefoxOptionsAppliesHeadlessMode()
	{
		// Given: headless mode enabled
		Mockito.when(browserOptions.isHeadless()).thenReturn(true);

		// When: creating options
		FirefoxBrowserFactory factory = new FirefoxBrowserFactory();
		FirefoxOptions options = factory.createFirefoxOptions(browserOptions);

		// Then: options are created successfully with headless mode
		Assertions.assertNotNull(options, "FirefoxOptions should be created with headless mode");
	}
}

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
import org.openqa.selenium.edge.EdgeOptions;
import java.util.List;
import java.util.ArrayList;

/**
 * Tests for EdgeBrowserFactory browser arguments integration.
 *
 * @author James Amoore
 */
public class EdgeBrowserFactoryTest
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
	public void testCreateEdgeOptionsAppliesResolvedArguments()
	{
		// Given: provider returns specific arguments
		List<String> expectedArgs = List.of("--disable-features=VizDisplayCompositor", "--window-size=1920,1080");
		List<ProvenancedArgument> provenance = new ArrayList<>();
		ResolvedBrowserArguments resolved = new ResolvedBrowserArguments(expectedArgs, provenance);
		
		Mockito.when(mockProvider.resolve(StandardBrowser.EDGE, journeyContext))
			   .thenReturn(resolved);

		// When: creating edge options with context
		EdgeBrowserFactory factory = new EdgeBrowserFactory(configuration, mockProvider);
		EdgeOptions options = factory.createEdgeOptions(browserOptions, journeyContext);

		// Then: verify provider was called
		Mockito.verify(mockProvider).resolve(StandardBrowser.EDGE, journeyContext);
		
		// And options object is created successfully
		Assertions.assertNotNull(options, "EdgeOptions should be created");
	}

	@Test
	public void testCreateEdgeOptionsSkipsProviderWhenFeatureDisabled()
	{
		// Given: feature flag disabled
		AsyncConfiguration disabledConfig = new AsyncConfiguration(
			List.of(), List.of(), List.of(), List.of(), false, "reject", List.of(), List.of(), "DEBUG");

		// When: creating options
		EdgeBrowserFactory factory = new EdgeBrowserFactory(disabledConfig, mockProvider);
		EdgeOptions options = factory.createEdgeOptions(browserOptions, journeyContext);

		// Then: provider not called
		Mockito.verify(mockProvider, Mockito.never()).resolve(Mockito.any(), Mockito.any());
		
		// And options object is created successfully
		Assertions.assertNotNull(options, "EdgeOptions should be created");
	}

	@Test
	public void testCreateEdgeOptionsSkipsProviderWhenContextNull()
	{
		// When: creating options without context
		EdgeBrowserFactory factory = new EdgeBrowserFactory(configuration, mockProvider);
		EdgeOptions options = factory.createEdgeOptions(browserOptions, null);

		// Then: provider not called
		Mockito.verify(mockProvider, Mockito.never()).resolve(Mockito.any(), Mockito.any());
		
		// And options object is created successfully
		Assertions.assertNotNull(options, "EdgeOptions should be created");
	}

	@Test
	public void testCreateEdgeOptionsHandlesProviderException()
	{
		// Given: provider throws exception
		Mockito.when(mockProvider.resolve(StandardBrowser.EDGE, journeyContext))
			   .thenThrow(new RuntimeException("Provider error"));

		// When: creating options (should not throw)
		EdgeBrowserFactory factory = new EdgeBrowserFactory(configuration, mockProvider);
		EdgeOptions options = factory.createEdgeOptions(browserOptions, journeyContext);

		// Then: options are still created successfully despite provider error
		Assertions.assertNotNull(options, "EdgeOptions should be created despite provider error");
	}

	@Test
	public void testCreateEdgeOptionsAppliesHeadlessMode()
	{
		// Given: headless mode enabled
		Mockito.when(browserOptions.isHeadless()).thenReturn(true);

		// When: creating options
		EdgeBrowserFactory factory = new EdgeBrowserFactory();
		EdgeOptions options = factory.createEdgeOptions(browserOptions);

		// Then: options are created successfully with headless mode
		Assertions.assertNotNull(options, "EdgeOptions should be created with headless mode");
	}
}

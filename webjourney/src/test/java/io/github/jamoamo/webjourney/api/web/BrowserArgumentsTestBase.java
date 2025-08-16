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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Base test infrastructure for consistent browser arguments test setup.
 * Provides standardized test configuration builders, mock utilities, and cleanup.
 *
 * @author James Amoore
 */
public abstract class BrowserArgumentsTestBase
{
    protected static final Set<String> DEFAULT_DENY_LIST = Set.of(
        "--user-data-dir", "--remote-debugging-port", "--disable-web-security"
    );
    
    protected static final Path TEST_DATA_DIR = Paths.get("target/test-data");
    private Path tempConfigFile;
    
    @BeforeEach
    void setupTestBase() throws IOException
    {
        // Create test data directory if it doesn't exist
        if (!Files.exists(TEST_DATA_DIR))
        {
            Files.createDirectories(TEST_DATA_DIR);
        }
    }
    
    @AfterEach
    void cleanupTestBase() throws IOException
    {
        // Clean up temporary config file if created
        if (tempConfigFile != null && Files.exists(tempConfigFile))
        {
            Files.delete(tempConfigFile);
            tempConfigFile = null;
        }
    }
    
    /**
     * Creates a standardized test configuration with specified arguments.
     */
    protected AsyncConfiguration createTestConfig(
        List<String> globalArgs, 
        List<String> chromeArgs, 
        boolean enableExtraArgs, 
        String validationMode)
    {
        return new AsyncConfiguration(
            globalArgs != null ? globalArgs : List.of(),
            chromeArgs != null ? chromeArgs : List.of(),
            List.of(), // firefox args
            List.of(), // edge args
            enableExtraArgs,
            validationMode != null ? validationMode : "warn",
            List.of("--user-data-dir"), // default deny list
            List.of(), // redaction extra keys
            "DEBUG" // log level
        );
    }
    
    /**
     * Creates a comprehensive test configuration with all browser types.
     */
    protected AsyncConfiguration createFullTestConfig()
    {
        return new AsyncConfiguration(
            List.of("--disable-background-timer-throttling"), // global
            List.of("--window-size=1920,1080"), // chrome
            List.of("-safe-mode"), // firefox  
            List.of("--disable-background-mode"), // edge
            true, // enableExtraArgs
            "warn", // validation mode
            List.of("--user-data-dir"), // deny list
            List.of("password", "token"), // redaction keys
            "DEBUG" // log level
        );
    }
    
    /**
     * Creates a mock journey context with browser arguments.
     */
    protected IJourneyContext createMockJourneyContext(
        List<String> globalOverrides, 
        List<String> browserOverrides)
    {
        IJourneyContext context = Mockito.mock(IJourneyContext.class);
        IJourneyBrowserArguments browserArgs = Mockito.mock(IJourneyBrowserArguments.class);
        
        // Setup journey context with browser arguments
        Mockito.when(context.getBrowserArguments()).thenReturn(browserArgs);
        
        if (globalOverrides != null)
        {
            Mockito.when(browserArgs.snapshotGlobal()).thenReturn(globalOverrides);
        }
        else
        {
            Mockito.when(browserArgs.snapshotGlobal()).thenReturn(List.of());
        }
        
        if (browserOverrides != null)
        {
            Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.CHROME)).thenReturn(browserOverrides);
            Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.FIREFOX)).thenReturn(browserOverrides);
            Mockito.when(browserArgs.snapshotForBrowser(StandardBrowser.EDGE)).thenReturn(browserOverrides);
        }
        else
        {
            Mockito.when(browserArgs.snapshotForBrowser(Mockito.any())).thenReturn(List.of());
        }
        
        return context;
    }
    
    /**
     * Creates a mock journey context with browser-specific arguments.
     */
    protected IJourneyContext createMockJourneyContext(
        List<String> globalOverrides, 
        Map<StandardBrowser, List<String>> browserSpecificOverrides)
    {
        IJourneyContext context = Mockito.mock(IJourneyContext.class);
        IJourneyBrowserArguments browserArgs = Mockito.mock(IJourneyBrowserArguments.class);
        
        // Setup journey context with browser arguments
        Mockito.when(context.getBrowserArguments()).thenReturn(browserArgs);
        
        if (globalOverrides != null)
        {
            Mockito.when(browserArgs.snapshotGlobal()).thenReturn(globalOverrides);
        }
        else
        {
            Mockito.when(browserArgs.snapshotGlobal()).thenReturn(List.of());
        }
        
        if (browserSpecificOverrides != null)
        {
            for (Map.Entry<StandardBrowser, List<String>> entry : browserSpecificOverrides.entrySet())
            {
                Mockito.when(browserArgs.snapshotForBrowser(entry.getKey())).thenReturn(entry.getValue());
            }
        }
        
        // Default empty lists for any browser not specified
        for (StandardBrowser browser : StandardBrowser.values())
        {
            if (browserSpecificOverrides == null || !browserSpecificOverrides.containsKey(browser))
            {
                Mockito.when(browserArgs.snapshotForBrowser(browser)).thenReturn(List.of());
            }
        }
        
        return context;
    }
    
    /**
     * Creates a mock browser options for testing.
     */
    protected IBrowserOptions createMockBrowserOptions()
    {
        IBrowserOptions options = Mockito.mock(IBrowserOptions.class);
        Mockito.when(options.isHeadless()).thenReturn(false);
        Mockito.when(options.acceptUnexpectedAlerts()).thenReturn(true);
        return options;
    }
    
    /**
     * Creates a fake environment function for testing.
     */
    protected Function<String, String> createFakeEnvironment(Map<String, String> envVars)
    {
        return key -> envVars.get(key);
    }
    
    /**
     * Creates a temporary YAML config file for testing.
     */
    protected Path createTempConfigFile(String content) throws IOException
    {
        tempConfigFile = Files.createTempFile(TEST_DATA_DIR, "test-config", ".yml");
        Files.write(tempConfigFile, content.getBytes());
        return tempConfigFile;
    }
    
    /**
     * Creates a standardized browser arguments provider for testing.
     */
    protected DefaultBrowserArgumentsProvider createTestProvider(
        Function<String, String> envProvider, 
        AsyncConfiguration config)
    {
        return new DefaultBrowserArgumentsProvider(envProvider, config);
    }
    
    /**
     * Creates a mock browser arguments provider that returns specified results.
     */
    protected DefaultBrowserArgumentsProvider createMockProvider(ResolvedBrowserArguments result)
    {
        DefaultBrowserArgumentsProvider mock = Mockito.mock(DefaultBrowserArgumentsProvider.class);
        Mockito.when(mock.resolve(Mockito.any(), Mockito.any())).thenReturn(result);
        return mock;
    }
}

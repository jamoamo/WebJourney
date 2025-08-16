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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Platform-specific tests for browser arguments handling.
 * Tests OS-specific path separators, security arguments, and environment handling.
 *
 * @author James Amoore
 */
public class PlatformSpecificTest extends BrowserArgumentsTestBase
{
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windows_pathSeparators_handledCorrectly()
    {
        // Given: Windows-style paths in environment variables
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--user-data-dir=C:\\Users\\Test\\Chrome,--log-file=C:\\Temp\\chrome.log",
            "WEBJOURNEY_CHROME_ARGS", "--disk-cache-dir=D:\\Cache\\Chrome"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--default-dir=C:\\Program Files\\WebJourney"), 
            List.of(), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--temp-dir=C:\\Temp\\Journey"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Windows path separators are preserved in the actual arguments
        List<String> args = resolved.getArguments();
        boolean foundWindowsPath = args.stream().anyMatch(arg -> 
            arg.contains("C:\\") && (arg.contains("Program Files") || arg.contains("Temp") || arg.contains("Cache")));
        assertTrue(foundWindowsPath, "Should find Windows-style paths with backslash separators. Actual: " + args);
        
        // Verify no path corruption occurred
        for (String arg : resolved.getArguments())
        {
            if (arg.contains("=") && arg.contains(":\\"))
            {
                assertFalse(arg.contains("/"), "Windows paths should not be converted to Unix format: " + arg);
                assertTrue(arg.contains("\\"), "Windows paths should retain backslashes: " + arg);
            }
        }
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windows_quotedPaths_preservedCorrectly()
    {
        // Given: Windows paths with spaces that need proper handling (using allowed args)
        AsyncConfiguration config = createTestConfig(
            List.of("--log-file=C:\\Program Files\\Google\\Chrome User Data\\chrome.log"), 
            List.of(), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(createFakeEnvironment(Map.of()), config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Windows paths with spaces are handled correctly
        List<String> args = resolved.getArguments();
        boolean foundLogFile = args.stream().anyMatch(arg -> 
            arg.startsWith("--log-file=") && arg.contains("Program Files"));
        assertTrue(foundLogFile, "Should find log-file argument with Program Files. Actual: " + args);
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    void linux_permissions_argumentsWork()
    {
        // Given: Linux-specific security arguments for containerized environments
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--no-sandbox,--disable-setuid-sandbox,--disable-dev-shm-usage",
            "WEBJOURNEY_CHROME_ARGS", "--disable-background-timer-throttling"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-gpu"), 
            List.of("--disable-software-rasterizer"), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--single-process"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Linux security arguments are applied
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--no-sandbox",
            "--disable-setuid-sandbox", 
            "--disable-dev-shm-usage",
            "--disable-background-timer-throttling",
            "--disable-gpu",
            "--disable-software-rasterizer",
            "--single-process"
        );
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    void linux_unixPaths_preservedCorrectly()
    {
        // Given: Unix-style paths in various arguments
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--user-data-dir=/home/user/.config/chrome,--log-file=/var/log/chrome.log",
            "WEBJOURNEY_CHROME_ARGS", "--disk-cache-dir=/tmp/chrome-cache"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--crash-dumps-dir=/var/crash"), 
            List.of(), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--extensions-dir=/opt/chrome-extensions"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Unix paths are preserved
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--user-data-dir=/home/user/.config/chrome",
            "--log-file=/var/log/chrome.log",
            "--disk-cache-dir=/tmp/chrome-cache",
            "--crash-dumps-dir=/var/crash",
            "--extensions-dir=/opt/chrome-extensions"
        );
        
        // Verify no path corruption occurred
        for (String arg : resolved.getArguments())
        {
            if (arg.contains("=") && arg.contains("/"))
            {
                assertFalse(arg.contains("\\"), "Unix paths should not contain backslashes: " + arg);
                assertTrue(arg.startsWith("--") && arg.contains("/"), "Unix paths should retain forward slashes: " + arg);
            }
        }
    }
    
    @Test
    @EnabledOnOs(OS.MAC)
    void mac_arguments_handledCorrectly()
    {
        // Given: macOS-specific configuration
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--disable-background-timer-throttling,--disable-renderer-backgrounding",
            "WEBJOURNEY_CHROME_ARGS", "--disable-backgrounding-occluded-windows"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-background-mode"), 
            List.of("--disable-features=TranslateUI"), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--force-color-profile=srgb"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify macOS arguments work correctly
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--disable-background-timer-throttling",
            "--disable-renderer-backgrounding",
            "--disable-backgrounding-occluded-windows",
            "--disable-background-mode",
            "--disable-features=TranslateUI",
            "--force-color-profile=srgb"
        );
    }
    
    @Test
    @EnabledOnOs(OS.MAC)
    void mac_pathsWithSpaces_handledCorrectly()
    {
        // Given: macOS paths with spaces (common in Applications folder)
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--user-data-dir=/Users/user/Library/Application Support/Chrome"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(List.of(), List.of(), true, "warn");
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify macOS paths with spaces are preserved
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--user-data-dir=/Users/user/Library/Application Support/Chrome"
        );
    }
    
    @Test
    void crossPlatform_argumentCompatibility_preserved()
    {
        // Given: Arguments that should work across all platforms
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--headless,--disable-gpu,--no-sandbox",
            "WEBJOURNEY_CHROME_ARGS", "--window-size=1920x1080,--lang=en-US"  // Use x instead of comma to avoid parsing issues
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-web-security", "--allow-running-insecure-content"), 
            List.of("--disable-features=VizDisplayCompositor"), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--remote-allow-origins=*"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify cross-platform arguments are preserved
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--headless",
            "--disable-gpu", 
            "--no-sandbox",
            "--window-size=1920x1080",  // Updated to match our fix above
            "--lang=en-US",
            "--disable-web-security",
            "--allow-running-insecure-content",
            "--disable-features=VizDisplayCompositor",
            "--remote-allow-origins=*"
        );
        
        // Verify argument count matches expectations
        assertEquals(9, resolved.getArguments().size(), "Should have exactly 9 cross-platform arguments");
    }
    
    @Test
    void platformSpecific_environmentVariableHandling_consistent()
    {
        // Given: Environment variables with platform-agnostic content
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--user-agent=WebJourney Test Agent,--lang=en-US",
            "WEBJOURNEY_CHROME_ARGS", "--disable-extensions,--incognito"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(List.of(), List.of(), true, "warn");
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify environment variables are parsed consistently across platforms
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--user-agent=WebJourney Test Agent",
            "--lang=en-US",
            "--disable-extensions",
            "--incognito"
        );
        
        // Verify all arguments come from environment source
        BrowserArgumentsTestUtils.assertSourceCounts(resolved, 0, 0, 4, 0);
        
        for (ProvenancedArgument arg : resolved.getProvenance())
        {
            assertEquals(BrowserArgumentSource.ENVIRONMENT, arg.source(),
                "All arguments should be from environment source: " + arg.key());
        }
    }
    
    @Test
    void unicode_argumentsPreserved_acrossPlatforms()
    {
        // Given: Arguments with Unicode characters
        Map<String, String> envVars = Map.of(
            "WEBJOURNEY_BROWSER_ARGS", "--lang=zh-CN,--user-agent=测试浏览器"
        );
        
        Function<String, String> envProvider = createFakeEnvironment(envVars);
        AsyncConfiguration config = createTestConfig(
            List.of("--accept-lang=ja,en-US;q=0.9,en;q=0.8"), 
            List.of(), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--custom-header=Мой заголовок"), 
            List.of()
        );
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Unicode characters are preserved
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--lang=zh-CN",
            "--user-agent=测试浏览器",
            "--accept-lang=ja,en-US;q=0.9,en;q=0.8",
            "--custom-header=Мой заголовок"
        );
        
        // Verify Unicode content is not corrupted
        for (String arg : resolved.getArguments())
        {
            if (arg.contains("="))
            {
                String value = arg.substring(arg.indexOf('=') + 1);
                if (value.matches(".*[\\u4e00-\\u9fff].*|.*[\\u0400-\\u04ff].*")) // Chinese or Cyrillic
                {
                    assertFalse(value.contains("?"), "Unicode characters should not be corrupted: " + arg);
                }
            }
        }
    }
}

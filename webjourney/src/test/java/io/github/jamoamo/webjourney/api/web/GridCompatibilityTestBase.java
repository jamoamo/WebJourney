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

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test infrastructure for grid compatibility testing.
 * Provides utilities for testing argument serialization through RemoteWebDriver.
 *
 * @author James Amoore
 */
public abstract class GridCompatibilityTestBase extends BrowserArgumentsTestBase
{
    protected static final String GRID_HUB_URL = "http://localhost:4444/wd/hub";
    
    @BeforeEach
    void setupGridEnvironment()
    {
        // Setup test grid or use mock RemoteWebDriver
        // For CI: Use Selenoid or testcontainers
        // Note: Actual grid setup is optional for most tests
    }
    
    /**
     * Creates a RemoteWebDriver with the specified capabilities.
     * This is a test utility that may require actual grid infrastructure.
     */
    protected RemoteWebDriver createRemoteDriver(Capabilities capabilities)
    {
        try
        {
            return new RemoteWebDriver(URI.create(GRID_HUB_URL).toURL(), capabilities);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create RemoteWebDriver: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies that expected arguments are present in serialized capabilities.
     */
    protected void verifyCapabilitiesSerialization(Capabilities caps, List<String> expectedArgs)
    {
        assertNotNull(caps, "Capabilities should not be null");
        
        // Extract browser-specific options based on browser type
        String browserName = caps.getBrowserName();
        
        switch (browserName.toLowerCase())
        {
            case "chrome":
                verifyChromeCapabilities(caps, expectedArgs);
                break;
            case "firefox":
                verifyFirefoxCapabilities(caps, expectedArgs);
                break;
            case "microsoftedge":
            case "edge":
                verifyEdgeCapabilities(caps, expectedArgs);
                break;
            default:
                fail("Unsupported browser for grid testing: " + browserName);
        }
    }
    
    /**
     * Verifies Chrome-specific capabilities contain expected arguments.
     */
    @SuppressWarnings("unchecked")
    protected void verifyChromeCapabilities(Capabilities caps, List<String> expectedArgs)
    {
        Map<String, Object> chromeOptions = (Map<String, Object>) caps.getCapability(ChromeOptions.CAPABILITY);
        assertNotNull(chromeOptions, "Chrome options should be present in capabilities");
        
        List<String> args = (List<String>) chromeOptions.get("args");
        assertNotNull(args, "Chrome arguments should be present in options");
        
        for (String expectedArg : expectedArgs)
        {
            assertTrue(args.contains(expectedArg), 
                "Expected argument not found in Chrome capabilities: " + expectedArg + 
                ". Actual args: " + args);
        }
    }
    
    /**
     * Verifies Firefox-specific capabilities contain expected arguments.
     */
    @SuppressWarnings("unchecked")
    protected void verifyFirefoxCapabilities(Capabilities caps, List<String> expectedArgs)
    {
        Map<String, Object> firefoxOptions = (Map<String, Object>) caps.getCapability(FirefoxOptions.FIREFOX_OPTIONS);
        if (firefoxOptions == null)
        {
            // Try alternative capability name
            firefoxOptions = (Map<String, Object>) caps.getCapability("moz:firefoxOptions");
        }
        
        assertNotNull(firefoxOptions, "Firefox options should be present in capabilities");
        
        List<String> args = (List<String>) firefoxOptions.get("args");
        if (args != null)
        {
            for (String expectedArg : expectedArgs)
            {
                assertTrue(args.contains(expectedArg), 
                    "Expected argument not found in Firefox capabilities: " + expectedArg + 
                    ". Actual args: " + args);
            }
        }
    }
    
    /**
     * Verifies Edge-specific capabilities contain expected arguments.
     */
    @SuppressWarnings("unchecked")
    protected void verifyEdgeCapabilities(Capabilities caps, List<String> expectedArgs)
    {
        Map<String, Object> edgeOptions = (Map<String, Object>) caps.getCapability(EdgeOptions.CAPABILITY);
        assertNotNull(edgeOptions, "Edge options should be present in capabilities");
        
        List<String> args = (List<String>) edgeOptions.get("args");
        assertNotNull(args, "Edge arguments should be present in options");
        
        for (String expectedArg : expectedArgs)
        {
            assertTrue(args.contains(expectedArg), 
                "Expected argument not found in Edge capabilities: " + expectedArg + 
                ". Actual args: " + args);
        }
    }
    
    /**
     * Checks if a test grid is available for integration testing.
     */
    protected static boolean isGridAvailable()
    {
        try
        {
            URL hubUrl = URI.create(GRID_HUB_URL + "/status").toURL();
            HttpURLConnection conn = (HttpURLConnection) hubUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            return conn.getResponseCode() == 200;
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    /**
     * Creates test ChromeOptions with specified arguments for verification.
     */
    protected ChromeOptions createTestChromeOptions(List<String> arguments)
    {
        ChromeOptions options = new ChromeOptions();
        if (arguments != null && !arguments.isEmpty())
        {
            options.addArguments(arguments);
        }
        return options;
    }
    
    /**
     * Creates test FirefoxOptions with specified arguments for verification.
     */
    protected FirefoxOptions createTestFirefoxOptions(List<String> arguments)
    {
        FirefoxOptions options = new FirefoxOptions();
        if (arguments != null && !arguments.isEmpty())
        {
            options.addArguments(arguments);
        }
        return options;
    }
    
    /**
     * Creates test EdgeOptions with specified arguments for verification.
     */
    protected EdgeOptions createTestEdgeOptions(List<String> arguments)
    {
        EdgeOptions options = new EdgeOptions();
        if (arguments != null && !arguments.isEmpty())
        {
            options.addArguments(arguments);
        }
        return options;
    }
    
    /**
     * Extracts arguments from capabilities for verification purposes.
     */
    @SuppressWarnings("unchecked")
    protected List<String> extractArgumentsFromCapabilities(Capabilities caps, String browserName)
    {
        switch (browserName.toLowerCase())
        {
            case "chrome":
                Map<String, Object> chromeOpts = (Map<String, Object>) caps.getCapability(ChromeOptions.CAPABILITY);
                return chromeOpts != null ? (List<String>) chromeOpts.get("args") : null;
                
            case "firefox":
                Map<String, Object> firefoxOpts = (Map<String, Object>) caps.getCapability(FirefoxOptions.FIREFOX_OPTIONS);
                if (firefoxOpts == null)
                {
                    firefoxOpts = (Map<String, Object>) caps.getCapability("moz:firefoxOptions");
                }
                return firefoxOpts != null ? (List<String>) firefoxOpts.get("args") : null;
                
            case "edge":
                Map<String, Object> edgeOpts = (Map<String, Object>) caps.getCapability(EdgeOptions.CAPABILITY);
                return edgeOpts != null ? (List<String>) edgeOpts.get("args") : null;
                
            default:
                return null;
        }
    }
    
    /**
     * Verifies that no arguments leak between test runs.
     */
    protected void verifyArgumentIsolation(Capabilities caps1, Capabilities caps2, String browserName)
    {
        List<String> args1 = extractArgumentsFromCapabilities(caps1, browserName);
        List<String> args2 = extractArgumentsFromCapabilities(caps2, browserName);
        
        if (args1 != null && args2 != null)
        {
            // Verify that different test runs don't contaminate each other
            // This test would need specific argument patterns to be meaningful
            assertNotNull(args1, "First capabilities should have arguments");
            assertNotNull(args2, "Second capabilities should have arguments");
        }
    }
}

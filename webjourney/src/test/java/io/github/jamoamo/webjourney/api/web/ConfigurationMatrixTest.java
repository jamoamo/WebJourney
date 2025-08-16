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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end configuration matrix tests across all configuration sources.
 * Tests precedence verification across YAML, environment, and per-journey sources.
 *
 * @author James Amoore
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigurationMatrixTest extends BrowserArgumentsTestBase
{
    private Map<String, String> environmentOverrides;
    
    public enum ConfigSource
    {
        YAML_ONLY, 
        ENV_ONLY, 
        JOURNEY_ONLY, 
        YAML_AND_ENV, 
        ENV_AND_JOURNEY, 
        YAML_AND_JOURNEY,
        ALL_SOURCES
    }
    
    @BeforeEach
    void setupEnvironmentVariables()
    {
        environmentOverrides = new HashMap<>();
    }
    
    @AfterEach
    void cleanupEnvironmentVariables()
    {
        environmentOverrides.clear();
    }
    
    @ParameterizedTest
    @EnumSource(ConfigSource.class)
    void configurationMatrix_precedenceRespected(ConfigSource source)
    {
        // Given: Configuration source setup
        setupConfigurationSource(source);
        
        DefaultBrowserArgumentsProvider provider = createProviderForSource(source);
        IJourneyContext journeyContext = createJourneyContextForSource(source);
        
        // When: resolving browser arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, journeyContext);
        
        // Then: verify expected arguments and precedence
        verifyExpectedBehaviorForSource(source, resolved);
    }
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windows_pathSeparators_handledCorrectly()
    {
        // Given: Windows-style paths in environment variables (using allowed args)
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--log-file=C:\\Users\\Test\\Chrome.log");
        environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--crash-dumps-dir=C:\\Temp\\chrome");
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        AsyncConfiguration config = createTestConfig(List.of(), List.of(), true, "warn");
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Windows paths are preserved
        assertTrue(resolved.getArguments().contains("--log-file=C:\\Users\\Test\\Chrome.log"),
            "Windows path separators should be preserved");
        assertTrue(resolved.getArguments().contains("--crash-dumps-dir=C:\\Temp\\chrome"),
            "Windows path separators should be preserved in Chrome-specific args");
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    void linux_permissions_argumentsWork()
    {
        // Given: Linux-specific security arguments
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--no-sandbox,--disable-setuid-sandbox");
        environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--disable-dev-shm-usage");
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        AsyncConfiguration config = createTestConfig(List.of(), List.of(), true, "warn");
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify Linux security arguments are applied
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved, 
            "--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage");
    }
    
    @Test
    @EnabledOnOs(OS.MAC)
    void mac_arguments_handledCorrectly()
    {
        // Given: macOS-specific arguments
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--disable-background-timer-throttling");
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-backgrounding-occluded-windows"), 
            List.of(), 
            true, 
            "warn"
        );
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, config);
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify macOS arguments work correctly
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--disable-background-timer-throttling", "--disable-backgrounding-occluded-windows");
    }
    
    @Test
    void fullPrecedenceChain_perJourneyWins()
    {
        // Given: All sources with same key, different values
        AsyncConfiguration yamlConfig = createTestConfig(
            List.of("--shared-key=yaml"), 
            List.of("--shared-key=chrome-yaml"), 
            true, 
            "warn"
        );
        
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--shared-key=env");
        environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--shared-key=chrome-env");
        
        // For now, use mock context since DefaultJourneyBrowserArguments.add() is not yet implemented
        IJourneyContext context = createMockJourneyContext(List.of("--shared-key=journey"), List.of());
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, yamlConfig);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify per-journey wins
        BrowserArgumentsTestUtils.assertArgumentValue(resolved, "--shared-key", "journey");
        BrowserArgumentsTestUtils.assertProvenanceCorrect(resolved, "--shared-key", BrowserArgumentSource.PER_JOURNEY);
    }
    
    @Test
    void precedenceWithoutPerJourney_envWins()
    {
        // Given: Config and environment sources (no per-journey)
        AsyncConfiguration yamlConfig = createTestConfig(
            List.of("--shared-key=yaml"), 
            List.of("--shared-key=chrome-yaml"), 
            true, 
            "warn"
        );
        
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--shared-key=env");
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of()); // No per-journey args
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, yamlConfig);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify environment wins over config
        BrowserArgumentsTestUtils.assertArgumentValue(resolved, "--shared-key", "env");
        BrowserArgumentsTestUtils.assertProvenanceCorrect(resolved, "--shared-key", BrowserArgumentSource.ENVIRONMENT);
    }
    
    @Test
    void configOnly_browserSpecificWinsOverGlobal()
    {
        // Given: Only configuration source with both global and browser-specific
        AsyncConfiguration yamlConfig = createTestConfig(
            List.of("--shared-key=global"), 
            List.of("--shared-key=chrome"), 
            true, 
            "warn"
        );
        
        IJourneyContext context = createMockJourneyContext(List.of(), List.of());
        
        Function<String, String> envProvider = createFakeEnvironment(Map.of());
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, yamlConfig);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify browser-specific wins over global within config
        BrowserArgumentsTestUtils.assertArgumentValue(resolved, "--shared-key", "chrome");
        BrowserArgumentsTestUtils.assertProvenanceCorrect(resolved, "--shared-key", BrowserArgumentSource.PER_BROWSER_CONFIG);
    }
    
    @Test
    void complexMixedSources_allSourcesRepresented()
    {
        // Given: Arguments from all sources with unique keys
        AsyncConfiguration yamlConfig = createTestConfig(
            List.of("--yaml-global", "--mixed-key=yaml"), 
            List.of("--yaml-chrome"), 
            true, 
            "warn"
        );
        
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-global,--mixed-key=env");
        environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--env-chrome");
        
        IJourneyContext context = createMockJourneyContext(
            List.of("--journey-global", "--mixed-key=journey"), 
            List.of("--journey-chrome")
        );
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, yamlConfig);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify all unique arguments are present and mixed-key has correct precedence
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--yaml-global", "--yaml-chrome", "--env-global", "--env-chrome",
            "--journey-global", "--journey-chrome");
        
        BrowserArgumentsTestUtils.assertArgumentValue(resolved, "--mixed-key", "journey");
        BrowserArgumentsTestUtils.assertProvenanceCorrect(resolved, "--mixed-key", BrowserArgumentSource.PER_JOURNEY);
        
        // Verify source counts
        BrowserArgumentsTestUtils.assertSourceCounts(resolved, 1, 1, 2, 3); // global, per-browser, env, per-journey
    }
    
    @Test
    void emptyConfiguration_noArgumentsProduced()
    {
        // Given: Empty configuration across all sources
        AsyncConfiguration emptyConfig = createTestConfig(List.of(), List.of(), true, "warn");
        IJourneyContext emptyContext = createMockJourneyContext(List.of(), List.of());
        Function<String, String> emptyEnv = createFakeEnvironment(Map.of());
        
        DefaultBrowserArgumentsProvider provider = createTestProvider(emptyEnv, emptyConfig);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, emptyContext);
        
        // Then: verify no arguments are produced
        assertTrue(resolved.getArguments().isEmpty(), "Empty configuration should produce no arguments");
        assertTrue(resolved.getProvenance().isEmpty(), "Empty configuration should produce no provenance");
    }
    
    @Test
    void configurationWithDenyList_argumentsFiltered()
    {
        // Given: Configuration with denied arguments
        AsyncConfiguration configWithDenied = new AsyncConfiguration(
            List.of("--allowed-global", "--user-data-dir=/tmp"), // global with denied arg
            List.of("--allowed-chrome"), // chrome
            List.of(), // firefox
            List.of(), // edge
            true, // enableExtraArgs
            "warn", // validation mode - should drop denied args
            List.of("--user-data-dir"), // deny list
            List.of(), // redaction keys
            "DEBUG" // log level
        );
        
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-allowed,--remote-debugging-port=9222");
        
        IJourneyContext context = createMockJourneyContext(List.of("--journey-allowed"), List.of());
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, configWithDenied);
        
        // When: resolving arguments
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Then: verify allowed arguments are present and denied arguments are filtered
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "--allowed-global", "--allowed-chrome", "--env-allowed", "--journey-allowed");
        
        BrowserArgumentsTestUtils.assertArgumentsDoNotContain(resolved,
            "--user-data-dir", "--remote-debugging-port");
    }
    
    @Test
    void firefoxBrowser_singleDashArgumentsPreserved()
    {
        // Given: Firefox-specific configuration
        AsyncConfiguration firefoxConfig = createTestConfig(
            List.of("-headless"), 
            List.of(), // chrome args (should not apply to Firefox)
            true, 
            "warn"
        );
        
        environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "-safe-mode");
        environmentOverrides.put("WEBJOURNEY_FIREFOX_ARGS", "-private");
        
        IJourneyContext context = createMockJourneyContext(List.of("-profile"), List.of());
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        DefaultBrowserArgumentsProvider provider = createTestProvider(envProvider, firefoxConfig);
        
        // When: resolving arguments for Firefox
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.FIREFOX, context);
        
        // Then: verify Firefox single-dash arguments are preserved
        BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
            "-headless", "-safe-mode", "-private", "-profile");
    }
    
    // Helper methods for configuration source setup
    
    private void setupConfigurationSource(ConfigSource source)
    {
        switch (source)
        {
            case YAML_ONLY:
                // Config will be set up in createProviderForSource
                clearEnvironmentVars();
                break;
            case ENV_ONLY:
                environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--env-chrome");
                break;
            case JOURNEY_ONLY:
                clearEnvironmentVars();
                // Journey args will be set up in createJourneyContextForSource
                break;
            case YAML_AND_ENV:
                environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                break;
            case ENV_AND_JOURNEY:
                environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                // Journey args will be added in createJourneyContextForSource
                break;
            case YAML_AND_JOURNEY:
                clearEnvironmentVars();
                // Both YAML and journey will be set up in respective methods
                break;
            case ALL_SOURCES:
                environmentOverrides.put("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                environmentOverrides.put("WEBJOURNEY_CHROME_ARGS", "--env-chrome");
                // YAML config and journey args will be set up in respective methods
                break;
        }
    }
    
    private DefaultBrowserArgumentsProvider createProviderForSource(ConfigSource source)
    {
        AsyncConfiguration config;
        
        switch (source)
        {
            case YAML_ONLY:
            case YAML_AND_ENV:
            case YAML_AND_JOURNEY:
            case ALL_SOURCES:
                config = createTestConfig(
                    List.of("--yaml-global"), 
                    List.of("--yaml-chrome"), 
                    true, 
                    "warn"
                );
                break;
            default:
                config = createTestConfig(List.of(), List.of(), true, "warn");
                break;
        }
        
        Function<String, String> envProvider = createFakeEnvironment(environmentOverrides);
        return createTestProvider(envProvider, config);
    }
    
    private IJourneyContext createJourneyContextForSource(ConfigSource source)
    {
        switch (source)
        {
            case JOURNEY_ONLY:
            case ENV_AND_JOURNEY:
            case YAML_AND_JOURNEY:
            case ALL_SOURCES:
                return createMockJourneyContext(
                    List.of("--journey-global"), 
                    List.of("--journey-chrome")
                );
            default:
                return createMockJourneyContext(List.of(), List.of());
        }
    }
    
    private void verifyExpectedBehaviorForSource(ConfigSource source, ResolvedBrowserArguments resolved)
    {
        switch (source)
        {
            case YAML_ONLY:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved, "--yaml-global", "--yaml-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 1, 1, 0, 0);
                break;
            case ENV_ONLY:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved, "--env-global", "--env-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 0, 0, 2, 0);
                break;
            case JOURNEY_ONLY:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved, "--journey-global", "--journey-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 0, 0, 0, 2);
                break;
            case YAML_AND_ENV:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved, 
                    "--yaml-global", "--yaml-chrome", "--env-global");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 1, 1, 1, 0);
                break;
            case ENV_AND_JOURNEY:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
                    "--env-global", "--journey-global", "--journey-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 0, 0, 1, 2);
                break;
            case YAML_AND_JOURNEY:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
                    "--yaml-global", "--yaml-chrome", "--journey-global", "--journey-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 1, 1, 0, 2);
                break;
            case ALL_SOURCES:
                BrowserArgumentsTestUtils.assertArgumentsContain(resolved,
                    "--yaml-global", "--yaml-chrome", "--env-global", "--env-chrome",
                    "--journey-global", "--journey-chrome");
                BrowserArgumentsTestUtils.assertSourceCounts(resolved, 1, 1, 2, 2);
                break;
        }
    }
    
    private void clearEnvironmentVars()
    {
        environmentOverrides.clear();
    }
}

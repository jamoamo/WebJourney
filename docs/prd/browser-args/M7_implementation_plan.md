# M7 Implementation Plan: Testing

> Source PRD: [browser-args.md](./browser-args.md)  
> Task Breakdown: [browser-args-task-breakdown.md](./browser-args-task-breakdown.md)  
> Design: [M0-2_design_note.md](./M0-2_design_note.md)

## Overview

Milestone 7 establishes comprehensive test coverage for the browser arguments feature across unit, integration, and end-to-end scenarios. The goal is >90% coverage for core modules with reliable verification of browser argument handling across local drivers, remote grids, and configuration sources.

**Total Effort**: 7-12 days  
**Dependencies**: M2-M6 (all core functionality implemented)  
**Key Focus**: Test reliability, coverage completeness, CI integration

## Tasks Implementation

### M7.1: Unit Tests for Parsing/Merging/Denylists/Redaction (2-3 days)

#### Overview
Create comprehensive unit test matrix covering all M2-M4 modules with high coverage and edge case handling.

#### Implementation Strategy

**Phase 1: Test Infrastructure Setup (0.5 day)**
```java
// Base test infrastructure for consistent setup
abstract class BrowserArgumentsTestBase {
    protected static final Set<String> DEFAULT_DENY_LIST = Set.of(
        "--user-data-dir", "--remote-debugging-port", "--disable-web-security"
    );
    
    protected AsyncConfiguration createTestConfig(
        List<String> globalArgs, List<String> chromeArgs, 
        boolean enableExtraArgs, String validationMode) {
        // Standardized test configuration builder
    }
    
    protected IJourneyContext createMockJourneyContext(
        List<String> globalOverrides, List<String> browserOverrides) {
        // Standardized journey context mock
    }
}
```

**Phase 2: Parser Module Tests (0.5 day)**

Extend existing `BrowserArgParserTest.java`:
```java
class BrowserArgParserTest {
    // Existing tests are good foundation
    
    @ParameterizedTest
    @ValueSource(strings = {
        "\"--flag with spaces\"",
        "'--single-quoted'", 
        "--key=\"value with spaces\"",
        "--proxy=host\\,port:8080"
    })
    void parse_quotingEdgeCases_handledCorrectly(String input) {
        // Test complex quoting scenarios
    }
    
    @Test
    void parse_windowsPathSeparators_preservedCorrectly() {
        // Test Windows-specific path handling
        String input = "--user-data-dir=C:\\Users\\Test\\Chrome";
        List<String> result = BrowserArgParser.parse(input);
        assertEquals(List.of("--user-data-dir=C:\\Users\\Test\\Chrome"), result);
    }
    
    @Test
    void normalize_firefoxSingleDash_preservedAsIs() {
        // Test Firefox single-dash arguments are not converted
        List<String> input = List.of("-headless", "--chrome-flag");
        List<String> result = BrowserArgParser.normalize(input);
        assertEquals(List.of("-headless", "--chrome-flag"), result);
    }
}
```

**Phase 3: Merge Module Tests (1 day)**

Extend existing `BrowserArgumentsMergeTest.java`:
```java
class BrowserArgumentsMergeTest {
    @Test
    void merge_complexPrecedenceScenario_correctOrder() {
        // Test realistic multi-source merge
        Map<BrowserArgumentSource, List<String>> sources = Map.of(
            GLOBAL_CONFIG, List.of("--global1", "--shared-key=global"),
            PER_BROWSER_CONFIG, List.of("--browser1", "--shared-key=browser"),
            ENVIRONMENT, List.of("--env1", "--shared-key=env"),
            PER_JOURNEY, List.of("--journey1", "--shared-key=journey")
        );
        
        List<ProvenancedArgument> result = BrowserArgumentsMerge.merge(sources);
        
        // Verify precedence: per-journey wins
        assertThat(result)
            .extracting(ProvenancedArgument::key, ProvenancedArgument::value, ProvenancedArgument::source)
            .containsExactly(
                tuple("--global1", null, GLOBAL_CONFIG),
                tuple("--browser1", null, PER_BROWSER_CONFIG),
                tuple("--env1", null, ENVIRONMENT),
                tuple("--journey1", null, PER_JOURNEY),
                tuple("--shared-key", "journey", PER_JOURNEY)
            );
    }
    
    @Test
    void merge_duplicateKeysWithinSameSource_lastWins() {
        // Test behavior when same source has duplicate keys
    }
    
    @Test
    void merge_keyOnlyVsKeyValue_conflictResolution() {
        // Test --flag vs --flag=value conflicts
    }
}
```

**Phase 4: Validation Module Tests (0.5 day)**

The existing `BrowserArgumentsValidatorTest.java` is comprehensive. Add:
```java
class BrowserArgumentsValidatorTest {
    @Test
    void validate_configuredDenyList_overridesDefaults() {
        // Test custom deny list configuration
    }
    
    @Test
    void validate_caseSensitivity_strictMatching() {
        // Ensure case-sensitive matching works correctly
    }
    
    @Test
    void validate_partialKeyMatches_notTriggered() {
        // Ensure --user-data-dir doesn't block --user-agent
    }
}
```

**Phase 5: Redaction Module Tests (0.5 day)**

Extend existing `BrowserArgumentsRedactorTest.java`:
```java
class BrowserArgumentsRedactorTest {
    @ParameterizedTest
    @ValueSource(strings = {
        "--proxy-server=http://user:pass@proxy.com:8080",
        "--proxy-server=https://username:password@secure-proxy.org",
        "--auth-server-whitelist=https://user:secret@internal.corp"
    })
    void redact_credentialsInUrls_maskedCorrectly(String input) {
        String result = BrowserArgumentsRedactor.redact(input);
        assertFalse(result.contains("pass"));
        assertFalse(result.contains("password"));
        assertFalse(result.contains("secret"));
        assertTrue(result.contains("***"));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "--api-key=abc123def456",
        "--password=secretpass",
        "--token=jwt.token.here",
        "--secret=mysecret"
    })
    void redact_keyValueSecrets_valuesHidden(String input) {
        String result = BrowserArgumentsRedactor.redact(input);
        assertEquals(input.split("=")[0] + "=***", result);
    }
    
    @Test
    void redact_nonSensitiveValues_unchanged() {
        String input = "--window-size=1920,1080";
        String result = BrowserArgumentsRedactor.redact(input);
        assertEquals(input, result);
    }
}
```

#### Test Coverage Goals
- **BrowserArgParser**: 100% line coverage, all edge cases
- **BrowserArgumentsMerge**: 95% coverage, focus on precedence logic
- **BrowserArgumentsValidator**: 100% coverage, all validation paths
- **BrowserArgumentsRedactor**: 95% coverage, all redaction patterns
- **DefaultBrowserArgumentsProvider**: 90% coverage, integration scenarios

#### Deliverables
1. Enhanced unit test suites with edge case coverage
2. Test utilities for consistent mocking and setup
3. Coverage report demonstrating >90% for target modules
4. Performance micro-tests for parsing/merging hot paths

---

### M7.2: Integration Tests for Local Drivers (DESCOPED) (2-4 days)

**Status**: Descoped per task breakdown, but design included for future reference.

#### Overview
Verify custom arguments are correctly applied to local Chrome, Firefox, and Edge drivers with runtime verification where possible.

#### Implementation Strategy

**Phase 1: Chrome Integration Tests (1 day)**
```java
@TestMethodOrder(OrderAnnotation.class)
class ChromeDriverIntegrationTest {
    
    @Test
    @Order(1)
    void chrome_customArguments_appliedToOptions() {
        // Verify ChromeOptions receives custom arguments
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-background-timer-throttling"),
            List.of("--window-size=1024,768")
        );
        
        ChromeBrowserFactory factory = new ChromeBrowserFactory(config, new DefaultBrowserArgumentsProvider());
        
        // Mock journey context with additional args
        IJourneyContext context = createMockJourneyContext(
            List.of("--headless"),
            List.of("--no-sandbox")
        );
        
        try (IBrowser browser = factory.createBrowser(mockBrowserOptions(), context)) {
            // Verify browser was created successfully
            assertNotNull(browser);
            
            // If possible, verify some arguments took effect
            // Note: Limited verification due to browser internals
        }
    }
    
    @Test
    @Order(2)
    void chrome_headlessFlag_verifiableInCapabilities() {
        // Test headless mode as it's verifiable via capabilities
    }
    
    @Test
    @Order(3) 
    void chrome_deniedArguments_preventsBrowserCreation() {
        // Test that denied arguments cause appropriate failure
    }
}
```

**Phase 2: Firefox Integration Tests (1 day)**
```java
class FirefoxDriverIntegrationTest {
    @Test
    void firefox_singleDashArguments_preservedCorrectly() {
        // Test Firefox-specific argument format (-headless vs --headless)
    }
    
    @Test
    void firefox_profileArguments_handledSafely() {
        // Test profile-related arguments with validation
    }
}
```

**Phase 3: Edge Integration Tests (1 day)**
```java
class EdgeDriverIntegrationTest {
    @Test
    void edge_chromeCompatibleArgs_workCorrectly() {
        // Test Chrome-compatible arguments work with Edge
    }
    
    @Test
    void edge_edgeSpecificArgs_appliedCorrectly() {
        // Test Edge-specific arguments
    }
}
```

**Phase 4: Cross-Browser Test Matrix (1 day)**
```java
@ParameterizedTest
@EnumSource(StandardBrowser.class)
void allBrowsers_basicArgumentsWork(StandardBrowser browserType) {
    // Test basic arguments across all supported browsers
}
```

#### Challenges and Mitigation
- **Driver availability**: Tests require browser binaries and drivers
- **CI environment**: May need headless-only testing
- **Verification limitations**: Many arguments not externally verifiable

#### Deliverables
1. Integration test suite for each supported browser
2. Test fixtures and utilities for browser setup/teardown
3. CI configuration for browser testing
4. Documentation of verification limitations

---

### M7.3: Integration Tests for Remote Grid (1-2 days)

#### Overview
Verify custom arguments propagate correctly through RemoteWebDriver and Selenium Grid with proper serialization.

#### Implementation Strategy

**Phase 1: Grid Compatibility Test Infrastructure (0.5 day)**
```java
class GridCompatibilityTestBase {
    protected static final String GRID_HUB_URL = "http://localhost:4444/wd/hub";
    
    @BeforeEach
    void setupGridEnvironment() {
        // Setup test grid or use mock RemoteWebDriver
        // For CI: Use Selenoid or testcontainers
    }
    
    protected RemoteWebDriver createRemoteDriver(Capabilities capabilities) {
        // Standardized remote driver creation
    }
    
    protected void verifyCapabilitiesSerialization(Capabilities caps, List<String> expectedArgs) {
        // Verify arguments present in serialized capabilities
    }
}
```

**Phase 2: Argument Serialization Tests (0.5 day)**
```java
class GridArgumentSerializationTest extends GridCompatibilityTestBase {
    
    @Test
    void chromeOptions_argumentsSerialized_correctlyToCapabilities() {
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-dev-shm-usage"),
            List.of("--window-size=1920,1080")
        );
        
        ChromeBrowserFactory factory = new ChromeBrowserFactory(config, new DefaultBrowserArgumentsProvider());
        IJourneyContext context = createMockJourneyContext(List.of("--headless"), List.of());
        
        ChromeOptions options = factory.createChromeOptions(mockBrowserOptions(), context);
        
        // Verify arguments are present in capabilities
        Map<String, Object> chromeOptionsMap = (Map<String, Object>) options.getCapability(ChromeOptions.CAPABILITY);
        List<String> args = (List<String>) chromeOptionsMap.get("args");
        
        assertThat(args).contains("--disable-dev-shm-usage", "--window-size=1920,1080", "--headless");
    }
    
    @Test
    void firefoxOptions_argumentsSerialized_correctlyToCapabilities() {
        // Similar test for Firefox
    }
    
    @Test
    void edgeOptions_argumentsSerialized_correctlyToCapabilities() {
        // Similar test for Edge
    }
}
```

**Phase 3: Grid Integration Tests (1 day)**
```java
class GridIntegrationTest extends GridCompatibilityTestBase {
    
    @Test
    @EnabledIf("isGridAvailable")
    void remoteChrome_customArguments_appliedSuccessfully() {
        AsyncConfiguration config = createTestConfig(
            List.of("--disable-background-timer-throttling"),
            List.of("--no-first-run")
        );
        
        ChromeBrowserFactory factory = new ChromeBrowserFactory(config, new DefaultBrowserArgumentsProvider());
        ChromeOptions options = factory.createChromeOptions(mockBrowserOptions(), mockJourneyContext());
        
        try (RemoteWebDriver driver = new RemoteWebDriver(new URL(GRID_HUB_URL), options)) {
            assertNotNull(driver);
            
            // Verify capabilities reflect custom arguments
            Capabilities caps = driver.getCapabilities();
            verifyCapabilitiesSerialization(caps, List.of("--disable-background-timer-throttling", "--no-first-run"));
            
            // Basic functionality test
            driver.get("https://example.com");
            assertThat(driver.getTitle()).isNotEmpty();
        }
    }
    
    @Test
    @EnabledIf("isGridAvailable")
    void gridNodeRejection_handledGracefully() {
        // Test behavior when grid rejects certain arguments
    }
    
    static boolean isGridAvailable() {
        // Check if test grid is running
        try {
            URL hubUrl = new URL(GRID_HUB_URL + "/status");
            HttpURLConnection conn = (HttpURLConnection) hubUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### Grid Test Environment Options
1. **Local Grid**: docker-compose with Selenium Grid
2. **Testcontainers**: Programmatic Grid container management  
3. **Mock Grid**: Unit test with mocked RemoteWebDriver
4. **CI Grid**: GitHub Actions with selenoid/selenium

```yaml
# docker-compose.yml for test grid
version: '3'
services:
  selenium-hub:
    image: selenium/hub:4.15.0
    ports:
      - "4444:4444"
  
  chrome-node:
    image: selenium/node-chrome:4.15.0
    environment:
      - HUB_HOST=selenium-hub
    depends_on:
      - selenium-hub
```

#### Deliverables
1. Grid compatibility test suite
2. Docker-based test grid configuration
3. Capability serialization verification
4. CI integration for grid testing

---

### M7.4: E2E Matrix Tests Across Config Sources (2-3 days)

#### Overview
Comprehensive end-to-end scenarios testing all configuration sources (YAML, environment, per-journey) across different precedence combinations and operating systems.

#### Implementation Strategy

**Phase 1: Configuration Matrix Test Framework (1 day)**
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigurationMatrixTest {
    
    @RegisterExtension
    static final EnvironmentVariableExtension envVars = new EnvironmentVariableExtension();
    
    enum ConfigSource {
        YAML_ONLY, ENV_ONLY, JOURNEY_ONLY, 
        YAML_AND_ENV, ENV_AND_JOURNEY, YAML_AND_JOURNEY,
        ALL_SOURCES
    }
    
    @ParameterizedTest
    @EnumSource(ConfigSource.class)
    void configurationMatrix_precedenceRespected(ConfigSource source) {
        setupConfigurationSource(source);
        
        // Create browser with configuration
        IBrowser browser = createBrowserWithConfiguration(source);
        
        try {
            // Verify browser creation succeeds
            assertNotNull(browser);
            
            // Verify expected arguments were applied (where verifiable)
            verifyExpectedBehavior(source, browser);
            
        } finally {
            if (browser != null) browser.exit();
        }
    }
    
    private void setupConfigurationSource(ConfigSource source) {
        switch (source) {
            case YAML_ONLY:
                setupYamlConfig("--yaml-global", "--yaml-chrome");
                clearEnvironmentVars();
                break;
            case ENV_ONLY:
                clearYamlConfig();
                envVars.set("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                envVars.set("WEBJOURNEY_CHROME_ARGS", "--env-chrome");
                break;
            case ALL_SOURCES:
                setupYamlConfig("--yaml-global", "--yaml-chrome");
                envVars.set("WEBJOURNEY_BROWSER_ARGS", "--env-global");
                envVars.set("WEBJOURNEY_CHROME_ARGS", "--env-chrome");
                // Journey args added during browser creation
                break;
        }
    }
}
```

**Phase 2: Platform-Specific Tests (1 day)**
```java
class PlatformSpecificTest {
    
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windows_pathSeparators_handledCorrectly() {
        envVars.set("WEBJOURNEY_BROWSER_ARGS", "--user-data-dir=C:\\Users\\Test\\Chrome");
        
        // Test Windows path handling
        verifyWindowsPathHandling();
    }
    
    @Test
    @EnabledOnOs(OS.LINUX)
    void linux_permissions_argumentsWork() {
        envVars.set("WEBJOURNEY_BROWSER_ARGS", "--no-sandbox,--disable-setuid-sandbox");
        
        // Test Linux-specific security arguments
        verifyLinuxSecurityHandling();
    }
    
    @Test
    @EnabledOnOs(OS.MAC)
    void mac_arguments_handledCorrectly() {
        // Test macOS-specific scenarios
    }
}
```

**Phase 3: Precedence Verification Tests (1 day)**
```java
class PrecedenceVerificationTest {
    
    @Test
    void fullPrecedenceChain_perJourneyWins() {
        // Setup all sources with same key, different values
        setupYamlConfig(List.of("--shared-key=yaml"), List.of());
        envVars.set("WEBJOURNEY_BROWSER_ARGS", "--shared-key=env");
        
        DefaultJourneyBrowserArguments journeyArgs = new DefaultJourneyBrowserArguments();
        journeyArgs.add(List.of("--shared-key=journey"));
        
        TestJourneyContext context = new TestJourneyContext(journeyArgs);
        DefaultBrowserArgumentsProvider provider = createProvider();
        
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Verify per-journey wins
        ProvenancedArgument sharedKey = findArgumentByKey(resolved, "--shared-key");
        assertEquals("journey", sharedKey.value());
        assertEquals(BrowserArgumentSource.PER_JOURNEY, sharedKey.source());
    }
    
    @Test
    void precedenceWithoutPerJourney_envWins() {
        setupYamlConfig(List.of("--shared-key=yaml"), List.of("--shared-key=chrome"));
        envVars.set("WEBJOURNEY_BROWSER_ARGS", "--shared-key=env");
        
        // No per-journey args
        TestJourneyContext context = new TestJourneyContext(new DefaultJourneyBrowserArguments());
        DefaultBrowserArgumentsProvider provider = createProvider();
        
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
        
        // Verify environment wins over config
        ProvenancedArgument sharedKey = findArgumentByKey(resolved, "--shared-key");
        assertEquals("env", sharedKey.value());
        assertEquals(BrowserArgumentSource.ENVIRONMENT, sharedKey.source());
    }
}
```

#### Test Data Management
```java
class TestConfigurationBuilder {
    public static AsyncConfiguration createFullConfiguration() {
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
    
    public static void setupTestYamlFile(String configContent) {
        // Create temporary YAML config file
    }
    
    public static void cleanupTestFiles() {
        // Clean up temporary config files
    }
}
```

#### CI Matrix Configuration
```yaml
# .github/workflows/browser-args-e2e.yml
name: Browser Arguments E2E Tests

on: [push, pull_request]

jobs:
  e2e-matrix:
    strategy:
      matrix:
        os: [ubuntu-latest, windows-latest, macos-latest]
        config-source: [yaml-only, env-only, journey-only, all-sources]
        browser: [chrome, firefox, edge]
    
    runs-on: ${{ matrix.os }}
    
    env:
      WEBJOURNEY_BROWSER_ARGS: "--test-env-global"
      WEBJOURNEY_CHROME_ARGS: "--test-env-chrome"
    
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Setup browsers
        run: |
          # Install browser binaries for testing
      
      - name: Run E2E matrix tests
        run: |
          mvn test -Dtest=ConfigurationMatrixTest -Dconfig.source=${{ matrix.config-source }} -Dbrowser.type=${{ matrix.browser }}
```

#### Deliverables
1. Comprehensive configuration matrix test suite
2. Platform-specific test scenarios
3. Precedence verification tests
4. CI matrix configuration for cross-platform testing
5. Test data management utilities

---

## Cross-Cutting Concerns

### Test Data Management
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDataManager {
    private static final Path TEST_DATA_DIR = Paths.get("target/test-data");
    
    @BeforeAll
    void setupTestData() {
        createTestDataDirectory();
        createSampleConfigurations();
    }
    
    @AfterAll
    void cleanupTestData() {
        deleteTestDataDirectory();
    }
    
    public Path createTemporaryConfig(String content) {
        // Create temporary config files for testing
    }
}
```

### Test Utilities
```java
class BrowserArgumentsTestUtils {
    public static void assertArgumentsContain(ResolvedBrowserArguments resolved, String... expectedArgs) {
        List<String> actualArgs = resolved.getArguments();
        for (String expected : expectedArgs) {
            assertTrue(actualArgs.contains(expected), 
                "Expected argument not found: " + expected + ". Actual: " + actualArgs);
        }
    }
    
    public static void assertProvenanceCorrect(ResolvedBrowserArguments resolved, String key, BrowserArgumentSource expectedSource) {
        ProvenancedArgument arg = findArgumentByKey(resolved, key);
        assertNotNull(arg, "Argument not found: " + key);
        assertEquals(expectedSource, arg.source(), "Wrong provenance for " + key);
    }
}
```

### Mock Infrastructure
```java
@TestComponent
class MockProviderFactory {
    public BrowserArgumentsProvider createMockProvider(ResolvedBrowserArguments result) {
        BrowserArgumentsProvider mock = Mockito.mock(BrowserArgumentsProvider.class);
        Mockito.when(mock.resolve(any(), any())).thenReturn(result);
        return mock;
    }
    
    public IJourneyContext createMockJourneyContext(List<String> globalArgs, Map<StandardBrowser, List<String>> browserArgs) {
        // Standardized mock journey context creation
    }
}
```

## Acceptance Criteria

### M7.1 Acceptance
- [ ] >90% line coverage for BrowserArgParser, BrowserArgumentsMerge, BrowserArgumentsValidator, BrowserArgumentsRedactor
- [ ] >85% coverage for DefaultBrowserArgumentsProvider integration scenarios  
- [ ] All edge cases tested: quoting, escaping, precedence conflicts, validation modes
- [ ] Test execution time <30 seconds for full unit test suite
- [ ] Zero flaky tests in CI environment

### M7.2 Acceptance (Descoped)
- [ ] Integration tests for Chrome, Firefox, Edge with custom arguments
- [ ] Verification of argument application where technically feasible
- [ ] Graceful handling of browser creation failures
- [ ] Cross-platform compatibility verified

### M7.3 Acceptance  
- [ ] Grid compatibility verified with local test grid
- [ ] Argument serialization through RemoteWebDriver confirmed
- [ ] Capability propagation tested and documented
- [ ] Grid failure scenarios handled appropriately

### M7.4 Acceptance
- [ ] Full configuration matrix tested across sources
- [ ] Precedence verification for all combination scenarios
- [ ] Platform-specific tests pass on Windows, Linux, macOS
- [ ] CI matrix configuration validates cross-platform behavior
- [ ] Zero configuration leakage between test scenarios

## Risk Mitigation

### Test Environment Challenges
1. **Browser driver availability**: Use webdriver-manager or similar for automatic driver management
2. **CI resource constraints**: Implement test categories (fast/slow) for selective execution
3. **Grid infrastructure**: Provide fallback to mock-based tests when grid unavailable
4. **Platform differences**: Use conditional test execution based on OS capabilities

### Test Reliability
1. **Timing issues**: Use explicit waits instead of sleeps
2. **Resource cleanup**: Implement comprehensive cleanup in @AfterEach/@AfterAll
3. **Test isolation**: Ensure no shared state between test methods
4. **Configuration pollution**: Use temporary files and environment variable isolation

### Coverage and Quality
1. **Coverage gaps**: Use JaCoCo reports to identify and address coverage holes
2. **Test maintenance**: Keep test utilities DRY and well-documented
3. **Performance**: Monitor test execution time and optimize slow tests
4. **Debugging**: Provide clear assertion messages and diagnostic output

## Timeline and Dependencies

**Day 1-3**: M7.1 Unit test matrix implementation  
**Day 4-5**: M7.3 Grid compatibility tests  
**Day 6-8**: M7.4 E2E configuration matrix tests  
**Day 9**: Integration, CI setup, and documentation  

**Critical Dependencies**:
- M2.1-M2.2: Parser and merge utilities
- M3.2: DefaultBrowserArgumentsProvider
- M4.1-M4.3: Validation, redaction, logging
- M5.1: Browser factory integration

**Parallel Work Opportunities**:
- M7.1 and M7.3 can be developed in parallel
- CI configuration can be developed alongside test implementation
- Documentation can be written concurrently with test development

## Deliverables Summary

1. **Enhanced Unit Test Suites**:
   - Comprehensive parser, merge, validation, redaction tests
   - Test utilities and base classes for consistency
   - Coverage reports demonstrating >90% target

2. **Grid Compatibility Tests**:
   - Argument serialization verification
   - RemoteWebDriver integration tests
   - Docker-based test grid infrastructure

3. **E2E Configuration Matrix**:
   - Cross-platform test scenarios
   - Multi-source precedence verification
   - CI matrix for automated validation

4. **Test Infrastructure**:
   - Standardized test utilities and mocks
   - Temporary file and environment management
   - Performance monitoring and optimization

5. **Documentation**:
   - Test strategy and coverage reports
   - CI setup and maintenance guides
   - Known limitations and workarounds

This comprehensive testing strategy ensures the browser arguments feature is robust, reliable, and maintainable across all supported platforms and configuration scenarios.

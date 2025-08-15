# M5 Implementation Plan — Selenium Integration

### Objective
Integrate browser arguments provider with Selenium WebDriver factories to apply resolved arguments to `ChromeOptions`, `FirefoxOptions`, and `EdgeOptions`. Ensure compatibility with remote Grid deployments and implement feature flag control.

### Scope Summary
- Apply arguments to local browser `*Options` instances via existing factories
- Ensure argument propagation works with `RemoteWebDriver`/Grid setups
- Wire feature flag `browser.enableExtraArgs` to bypass provider when disabled
- Maintain backward compatibility and verify serialization integrity

### Current State Snapshot
- `DefaultBrowserArgumentsProvider` is complete with validation, redaction, and logging (M3-M4)
- `ChromeBrowserFactory` exists and creates `ChromeOptions`
- Firefox and Edge factories may not exist yet; create as needed
- `BrowserPool` manages browser lifecycle and driver creation
- Remote Grid support exists but argument integration needs verification

### Design and Components

#### 1. Local Browser Integration (M5.1)

**ChromeBrowserFactory Updates:**
- Locate `createChromeOptions(IBrowserOptions)` method
- Inject `BrowserArgumentsProvider` dependency
- Apply resolved arguments after existing hardcoded defaults
- Implementation pattern:
  ```java
  ChromeOptions options = new ChromeOptions();
  // ... existing hardcoded setup ...
  
  if (isExtraArgsEnabled()) {
      ResolvedBrowserArguments resolved = browserArgumentsProvider.resolve(
          StandardBrowser.CHROME, journeyContext);
      options.addArguments(resolved.arguments());
  }
  
  return options;
  ```

**Firefox and Edge Factory Creation:**
- Create `FirefoxBrowserFactory` if it doesn't exist
- Create `EdgeBrowserFactory` if it doesn't exist  
- Follow same pattern as Chrome integration
- Use `FirefoxOptions.addArguments()` and `EdgeOptions.addArguments()`

**Integration Points:**
- Hook into existing browser creation pipeline in `BrowserPool` or equivalent
- Ensure `JourneyContext` is available at factory call sites
- Provider instance managed as singleton or injected dependency

#### 2. Remote Grid Compatibility (M5.2)

**Capability Propagation:**
- Verify arguments flow correctly through `Capabilities` for `RemoteWebDriver`
- Ensure no serialization/deserialization loss of custom arguments
- Test with common Grid setups (Selenium Grid, potential cloud providers)

**Serialization Verification:**
- Arguments must survive JSON serialization to Grid nodes
- Verify `DesiredCapabilities` vs `MutableCapabilities` handling
- Check for character encoding issues in argument values

**Grid-Specific Considerations:**
- Arguments applied on Grid node, not client side
- Verify Grid node browser versions support provided arguments
- Handle Grid-specific argument rejection gracefully

#### 3. Feature Flag Implementation (M5.3)

**Configuration Key:**
- `browser.enableExtraArgs` (boolean, default: true for new feature)
- Consider backward compatibility: default may be false initially

**Integration Logic:**
- Check flag before calling provider in each factory
- When disabled: skip provider entirely, use existing hardcoded behavior
- Ensure zero overhead when feature is disabled

**Flag Access:**
- Read from `AsyncConfiguration` or equivalent config holder
- Cache flag value per journey context if config is expensive to access
- Handle missing/invalid config gracefully (default to safe behavior)

### Implementation Plan

#### Phase 1: Chrome Integration
1. **Locate Integration Point**
   - Find `ChromeBrowserFactory.createChromeOptions()` method
   - Identify how `JourneyContext` can be accessed at this call site
   - Determine dependency injection pattern for `BrowserArgumentsProvider`

2. **Provider Integration**
   - Add provider dependency to factory constructor or method
   - Implement feature flag check
   - Call `provider.resolve(CHROME, journeyContext)` when enabled
   - Apply `options.addArguments(resolved.arguments())`

3. **Testing**
   - Unit tests: verify `ChromeOptions` receives expected arguments
   - Integration tests: launch Chrome with custom args and verify reflection
   - Feature flag tests: ensure no behavior change when disabled

#### Phase 2: Firefox and Edge Factories
1. **Factory Creation/Updates**
   - Create `FirefoxBrowserFactory` if missing, following Chrome pattern
   - Create `EdgeBrowserFactory` if missing, following Chrome pattern
   - Apply same integration pattern as Chrome

2. **Cross-Browser Testing**
   - Verify arguments work correctly for each browser type
   - Test browser-specific argument formats (Chrome `--arg`, Firefox `-arg`)
   - Ensure per-browser configuration works as designed

#### Phase 3: Remote Grid Support
1. **Grid Integration Verification**
   - Set up local Selenium Grid for testing
   - Verify custom arguments propagate through `RemoteWebDriver`
   - Test argument serialization in `Capabilities`

2. **Grid Compatibility Testing**
   - Test with different Grid versions
   - Verify behavior with Grid node rejections
   - Document any Grid-specific limitations

#### Phase 4: Feature Flag Wiring
1. **Configuration Access**
   - Wire `browser.enableExtraArgs` into factory classes
   - Implement efficient flag checking (avoid repeated config reads)
   - Handle config loading errors gracefully

2. **Backward Compatibility Verification**
   - Run existing test suites with feature disabled
   - Verify no behavior change when no custom args present
   - Test with feature enabled but no configuration

### Testing Strategy

#### Unit Tests
```java
@Test
void shouldApplyResolvedArgumentsToChrome() {
    // Given: provider returns specific arguments
    when(provider.resolve(CHROME, context))
        .thenReturn(new ResolvedBrowserArguments(
            List.of("--headless", "--disable-gpu"), 
            provenance));
    
    // When: creating chrome options
    ChromeOptions options = factory.createChromeOptions(browserOptions);
    
    // Then: arguments are applied
    assertThat(options.getArguments())
        .contains("--headless", "--disable-gpu");
}

@Test
void shouldSkipProviderWhenFeatureDisabled() {
    // Given: feature flag disabled
    when(config.getBrowserEnableExtraArgs()).thenReturn(false);
    
    // When: creating options
    ChromeOptions options = factory.createChromeOptions(browserOptions);
    
    // Then: provider not called
    verify(provider, never()).resolve(any(), any());
}
```

#### Integration Tests
```java
@Test
void shouldLaunchChromeWithCustomArguments() {
    // Given: journey context with custom args
    journeyContext.getBrowserArguments()
        .add(List.of("--window-size=1920,1080"));
    
    // When: creating webdriver
    WebDriver driver = createDriver(CHROME, journeyContext);
    
    // Then: verify argument reflection in capabilities
    Capabilities caps = ((HasCapabilities) driver).getCapabilities();
    assertThat(caps.getCapability("goog:chromeOptions"))
        .toString().contains("--window-size=1920,1080");
}
```

#### Grid Integration Tests
```java
@Test
void shouldPropagateArgumentsToRemoteDriver() {
    // Given: remote grid setup with custom args
    setupRemoteGrid();
    journeyContext.getBrowserArguments().add(List.of("--headless"));
    
    // When: creating remote driver
    RemoteWebDriver driver = createRemoteDriver(gridUrl, CHROME, journeyContext);
    
    // Then: arguments propagated through capabilities
    Capabilities caps = driver.getCapabilities();
    // Verify serialization preserved arguments
}
```

### Configuration Integration

#### Required Config Keys
```yaml
browser:
  enableExtraArgs: true          # feature flag
  args: []                       # existing global config
  chrome:
    args: []                     # existing per-browser config
  # ... other existing config
```

#### Backward Compatibility
- When `enableExtraArgs: false`: completely bypass provider, zero overhead
- When `enableExtraArgs: true` but no args configured: provider returns empty list
- Existing hardcoded arguments in factories remain unchanged unless overridden

### Error Handling and Edge Cases

#### Provider Errors
- Validation failures (deny-list violations): bubble up as configuration errors
- Parsing failures: log warning and continue with empty arguments
- Provider unavailable: log warning and continue with existing behavior

#### Browser-Specific Failures
- Invalid arguments for specific browser: let browser fail naturally
- Unsupported arguments: browser will ignore or warn, don't interfere
- Grid node rejection: log warning but don't fail journey creation

#### Configuration Errors
- Invalid feature flag value: default to safe behavior (false)
- Missing configuration: use safe defaults throughout
- Malformed argument strings: log parsing errors, skip invalid tokens

### Dependencies and Prerequisites
- M3.2: `DefaultBrowserArgumentsProvider` implementation complete
- M4.x: Validation, redaction, and logging implemented
- Existing browser factory infrastructure
- Configuration system from M1.x tasks

### Integration Points Map
```
JourneyContext
    ↓
BrowserPool/DriverManager
    ↓
ChromeBrowserFactory.createChromeOptions()
    ↓ (if enableExtraArgs)
BrowserArgumentsProvider.resolve()
    ↓
ChromeOptions.addArguments()
    ↓
WebDriver creation
```

### Acceptance Criteria

#### M5.1 Acceptance
- [ ] `ChromeOptions`, `FirefoxOptions`, `EdgeOptions` receive resolved arguments
- [ ] Arguments applied after existing defaults (override behavior works)
- [ ] Unit tests verify `*Options.getArguments()` contains expected values
- [ ] Integration tests launch browsers with custom arguments successfully

#### M5.2 Acceptance  
- [ ] Arguments propagate correctly through `RemoteWebDriver`
- [ ] No serialization loss in Grid communication
- [ ] Integration test against local Grid passes
- [ ] Documentation covers any Grid-specific limitations

#### M5.3 Acceptance
- [ ] Feature flag `browser.enableExtraArgs` controls provider usage
- [ ] When disabled: zero overhead, existing behavior unchanged
- [ ] When enabled: provider integration works as designed
- [ ] Default flag value decided and documented

### Effort Estimate
- **M5.1**: 1-2 days (Chrome integration + Firefox/Edge factories + local tests)
- **M5.2**: 1-2 days (Grid compatibility verification + tests)  
- **M5.3**: 0.5-1 day (Feature flag wiring + backward compatibility tests)

### Risks and Mitigations
- **Browser factory locations unclear**: Use codebase search to locate integration points
- **JourneyContext not available**: Modify factory signatures or dependency injection
- **Grid compatibility issues**: Start with local Grid, document limitations
- **Performance overhead**: Minimize provider calls, cache results where safe
- **Argument conflicts**: Let browser behavior determine precedence, document expectations

### Rollout Strategy
1. **Chrome first**: Validate integration pattern works correctly
2. **Firefox/Edge**: Apply proven pattern to additional browsers  
3. **Grid testing**: Verify remote compatibility doesn't break
4. **Feature flag**: Default to enabled after thorough testing
5. **Documentation**: Update user guides with examples and caveats

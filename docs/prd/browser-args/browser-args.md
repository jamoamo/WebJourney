# PRD: Additional Browser Arguments Support

## 1. Overview

This document specifies the requirements for a mechanism to allow users to provide additional command-line arguments when launching a browser instance (Chrome, Firefox, Edge). This will enable fine-grained control over browser behavior for testing, performance tuning, and diagnostics.

### Problem Statement

Users need a consistent way to pass custom browser flags (e.g., `--headless=new`, `--proxy-server=...`, `--disable-gpu`, `--window-size=1920,1080`) to the underlying WebDriver. The current framework lacks a unified, layered, and safe mechanism to supply these arguments, limiting flexibility for CI/CD, performance testing, and local development scenarios.

### Goals

- Provide a unified configuration API to pass extra browser arguments.
- Support global, per-browser, and per-journey argument overrides with clear precedence.
- Correctly map arguments to Selenium's `ChromeOptions`, `EdgeOptions`, and `FirefoxOptions`.
- Validate, sanitize, and log the final resolved arguments for observability and security.
- Ensure full compatibility with both local driver execution and remote Selenium Grid.
- Maintain 100% backward compatibility for existing users.

### Non-Goals

- Managing browser preferences beyond command-line flags (e.g., Firefox `about:config` settings).
- Providing a curated or opinionated set of default flags beyond what Selenium provides.

## 2. Requirements

### Functional Requirements

#### FR-1: Configuration Layers

The system must support argument configuration from multiple sources:

- **Global Configuration**: A default set of arguments applicable to all browser types.
- **Per-Browser Configuration**: Arguments specific to a browser type (Chrome, Firefox, Edge) that supplement or override global settings.
- **Environment Variables**: A mechanism to supply arguments via environment variables for CI/CD integration.
- **Per-Journey Override**: An API to programmatically add or override arguments for a single journey execution.

#### FR-2: Precedence Rules

Argument merging must follow a deterministic order of precedence (highest to lowest):
1.  Per-Journey Override
2.  Environment Variables
3.  Per-Browser Configuration
4.  Global Configuration

Arguments from a higher precedence layer supplement those from lower layers. If the same argument is defined in multiple layers, the value from the highest precedence layer is used.

#### FR-3: Selenium Integration

The resolved arguments must be correctly applied to the corresponding Selenium `*Options` class before a browser session is created:
- `ChromeOptions.addArguments(List<String>)`
- `EdgeOptions.addArguments(List<String>)`
- `FirefoxOptions.addArguments(List<String>)`

#### FR-4: Observability & Logging

The final, resolved list of arguments for each browser launch must be logged at an appropriate level (e.g., INFO or DEBUG), including the source of each argument for easier troubleshooting.

#### FR-5: Validation and Sanitization

- The system must provide a configurable deny-list to block known-dangerous flags (e.g., `--user-data-dir`).
- Sensitive values within arguments (e.g., proxy credentials) must be redacted in logs.
- Input strings should be normalized to handle whitespace and quotes correctly across different operating systems.

### Non-Functional Requirements

- **NFR-1: Performance**: The mechanism should introduce negligible overhead (<1ms) per browser launch when no custom arguments are configured.
- **NFR-2: Thread Safety**: Argument resolution must be thread-safe to support concurrent journey execution via the `BrowserPool`.
- **NFR-3: Backward Compatibility**: Existing journey executions must continue to work without any changes.

## 3. Design and Implementation

### Configuration Model

#### YAML/Properties

```yaml
# In YAML configuration
browser:
  args: # Global arguments
    - --headless=new
  chrome:
    args: # Chrome-specific arguments
      - --disable-gpu
      - --window-size=1920,1080
  firefox:
    args:
      - -headless
```

#### Environment Variables

Arguments can be provided as comma-separated strings.
```shell
# For POSIX shells
export WEBJOURNEY_BROWSER_ARGS="--headless=new"
export WEBJOURNEY_CHROME_ARGS="--disable-gpu,--window-size=1920,1080"

# For Windows PowerShell
$env:WEBJOURNEY_BROWSER_ARGS="--headless=new"
```

#### Programmatic API (Per-Journey)

The `AsyncJourneyContext` will be extended to allow per-journey overrides.

```java
// Example of a per-journey override
journeyContext.getBrowserArguments().addForBrowser(
    BrowserType.CHROME,
    List.of("--disable-web-security", "--remote-allow-origins=*")
);
```

### Core Components

- **`BrowserArgumentsProvider`**: An interface responsible for resolving the final list of arguments based on all configuration sources and precedence rules.
- **`DefaultBrowserArgumentsProvider`**: The default implementation that reads from `AsyncConfiguration`, environment variables, and the `AsyncJourneyContext`.
- **`AsyncConfiguration`**: Extended to hold the global and per-browser argument lists from YAML/properties files.
- **`BrowserPool` / Driver Factory**: Modified to invoke the `BrowserArgumentsProvider` and apply the resolved arguments to the `*Options` object before creating a `WebDriver` instance.

## 4. Test Plan

- **Unit Tests**:
    - Verify argument merging logic, including precedence and de-duplication.
    - Test the argument parser with various quoting and spacing styles.
    - Test the validation and deny-list logic.
- **Integration Tests**:
    - Launch each browser type with custom flags and verify they are applied.
    - Test concurrent journeys with different per-journey overrides to ensure isolation.
    - Verify that arguments are passed correctly to a remote Selenium Grid.
- **End-to-End Tests**:
    - Run a suite of journeys with a matrix of configurations (YAML, env vars) on different operating systems (Windows, Linux).

## 5. Risks and Mitigation

- **Risk**: Incorrect argument parsing, especially with quoted values on Windows.
  - **Mitigation**: Implement a robust parsing utility with comprehensive unit tests for edge cases.
- **Risk**: Users inadvertently supplying dangerous flags that compromise security.
  - **Mitigation**: Implement a default deny-list for high-risk flags. Make the list configurable so advanced users can override it if necessary.
- **Risk**: Sensitive data (e.g., passwords in proxy URLs) being exposed in logs.
  - **Mitigation**: Implement redaction logic for known patterns before logging.

## 6. Rollout and Backward Compatibility

The feature will be purely additive. All new configuration keys and APIs will be opt-in. No existing user journeys will be affected. The feature can be disabled globally via a feature flag (`browser.enableExtraArgs=false`) as a safety measure.


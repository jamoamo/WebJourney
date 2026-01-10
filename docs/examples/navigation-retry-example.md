# Navigation Retry Example

This document demonstrates how to handle environmental navigation errors with automatic retry logic in WebJourney.

## Overview

WebJourney now provides automatic retry logic for navigation operations that fail due to environmental issues like network connectivity problems. When a connection-related error occurs (e.g., `ERR_CONNECTION_REFUSED`), the framework will automatically retry the operation up to a configurable number of times.

## Exception Hierarchy

- `XNavigationError` - Base exception for navigation errors
  - `XEnvironmentalNavigationError` - Specific exception for environmental/network errors that have been retried

## Automatic Retry Behavior

### Default Behavior

By default, all navigation operations (`navigateToUrl`, `navigateBack`, `navigateForward`) will:
- Retry up to **3 times** for environmental errors
- Wait **1 second** between retry attempts
- Throw `XEnvironmentalNavigationError` if all retries fail
- Throw `XNavigationError` immediately for non-environmental errors

### Detected Environmental Errors

The following error patterns are automatically detected and trigger retry logic:
- `ERR_CONNECTION_REFUSED`
- `ERR_CONNECTION_RESET`
- `ERR_CONNECTION_CLOSED`
- `ERR_CONNECTION_TIMED_OUT`
- `ERR_NETWORK_CHANGED`
- `ERR_INTERNET_DISCONNECTED`
- `ERR_TIMED_OUT`
- `ERR_NAME_NOT_RESOLVED`
- `ERR_PROXY_CONNECTION_FAILED`
- Any `net::ERR_*` error containing CONNECTION, TIMEOUT, or NETWORK keywords

## Usage Examples

### Basic Usage (with default retry settings)

```java
try {
    IWebPage page = window.navigateToUrl(new URL("https://example.com"));
    // Navigation succeeded (possibly after retries)
} catch (XEnvironmentalNavigationError ex) {
    // Environmental error occurred after all retry attempts
    System.err.println("Failed after " + ex.getAttemptsMade() + " attempts");
    System.err.println("Error: " + ex.getMessage());
    
    // The consuming application can decide to retry the entire journey
    // since this indicates a transient environmental issue
} catch (XNavigationError ex) {
    // Non-environmental navigation error (not retried)
    // This indicates a problem that won't be fixed by retrying
}
```

### Customizing Retry Configuration

To customize the retry behavior for a specific window, you can configure the retry settings when creating the browser:

```java
// This would be configured in the browser factory or initialization code
// (The exact API for exposing this configuration is pending)

NavigationRetryConfig config = new NavigationRetryConfig(
    5,      // maxAttempts - retry up to 5 times
    2000    // retryDelayMillis - wait 2 seconds between retries
);

// Apply to window (internal API - needs to be exposed)
window.setRetryConfig(config);
```

### Handling Retries at the Journey Level

```java
public void runJourneyWithRetry(int maxJourneyRetries) {
    int journeyAttempt = 0;
    boolean success = false;
    
    while (!success && journeyAttempt < maxJourneyRetries) {
        journeyAttempt++;
        try {
            // Run your journey
            IWebPage page = window.navigateToUrl(new URL("https://example.com"));
            // ... perform journey steps ...
            success = true;
        } catch (XEnvironmentalNavigationError ex) {
            // Environmental error - the framework already retried at the navigation level
            // Consider retrying the entire journey
            if (journeyAttempt < maxJourneyRetries) {
                System.out.println("Journey failed due to environmental error. " +
                                   "Retrying journey (attempt " + (journeyAttempt + 1) + 
                                   " of " + maxJourneyRetries + ")");
                // Optionally wait before retrying the journey
                Thread.sleep(5000);
            } else {
                throw ex; // Max journey retries reached
            }
        }
    }
}
```

## Benefits

1. **Automatic Recovery**: Transient network issues are automatically handled without manual intervention
2. **Clear Error Signaling**: `XEnvironmentalNavigationError` clearly indicates that the failure was environmental, helping consuming applications make informed retry decisions
3. **Configurable**: Retry behavior can be tuned for different environments (e.g., more retries in unstable networks)
4. **Detailed Logging**: All retry attempts are logged with WARN level, making it easy to diagnose intermittent issues
5. **Smart Detection**: Only environmental errors are retried; logic errors fail fast

## Logging Output

When retries occur, you'll see log messages like:

```
[WARN] Window [MainWindow] navigateToUrl(https://example.com) failed with environmental error on attempt 1: 
       org.openqa.selenium.WebDriverException: unknown error: net::ERR_CONNECTION_REFUSED. Retrying...
[INFO] Window [MainWindow] navigateToUrl(https://example.com) succeeded on attempt 2
```

If all retries fail:

```
[ERROR] Window [MainWindow] navigateToUrl(https://example.com) failed after 3 attempts with environmental error
```

## Future Enhancements

Potential future enhancements could include:
- Configuration via properties/YAML files
- Per-browser retry settings
- Exponential backoff for retry delays
- Configurable error pattern detection
- Retry metrics and monitoring hooks


# Navigation Retry Solution

## Problem

The application was encountering `org.openqa.selenium.WebDriverException: unknown error: net::ERR_CONNECTION_REFUSED` errors during navigation operations (specifically in `SeleniumWindow.navigateToUrl`). These errors are environmental/transient in nature and should be handled with automatic retries or specific error signaling to allow the consuming application to retry the journey.

## Solution Overview

The solution implements a comprehensive retry mechanism with the following components:

### 1. New Exception Type: `XEnvironmentalNavigationError`

**Location**: `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/XEnvironmentalNavigationError.java`

A new exception class that extends `XNavigationError` to specifically indicate environmental/network-related errors that have been retried.

**Key Features**:
- Tracks the number of attempts made before failure
- Clearly signals to consuming applications that the error is environmental
- Includes the original exception as the cause for debugging

**Usage**:
```java
try {
    IWebPage page = window.navigateToUrl(url);
} catch (XEnvironmentalNavigationError ex) {
    // Error occurred due to environmental issues after retry attempts
    System.out.println("Failed after " + ex.getAttemptsMade() + " attempts");
    // Consider retrying the entire journey
}
```

### 2. Retry Configuration: `NavigationRetryConfig`

**Location**: `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/NavigationRetryConfig.java`

A configuration class that controls retry behavior.

**Default Configuration**:
- Maximum attempts: **3**
- Retry delay: **1000 ms** (1 second)

**Customization**:
```java
NavigationRetryConfig config = new NavigationRetryConfig(
    5,      // maxAttempts
    2000    // retryDelayMillis
);
window.setRetryConfig(config);
```

### 3. Enhanced `SeleniumWindow` Class

**Location**: `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/SeleniumWindow.java`

**Changes Made**:

#### a. Environmental Error Detection

The `isEnvironmentalError()` method detects connection-related errors:
- `ERR_CONNECTION_REFUSED`
- `ERR_CONNECTION_RESET`
- `ERR_CONNECTION_CLOSED`
- `ERR_CONNECTION_TIMED_OUT`
- `ERR_NETWORK_CHANGED`
- `ERR_INTERNET_DISCONNECTED`
- `ERR_TIMED_OUT`
- `ERR_NAME_NOT_RESOLVED`
- `ERR_PROXY_CONNECTION_FAILED`
- Any `net::ERR_*` containing CONNECTION, TIMEOUT, or NETWORK

#### b. Retry Logic

The `executeWithRetry()` method implements the retry logic:
1. Attempts the navigation operation
2. If it fails with an environmental error and max attempts not reached:
   - Logs a warning
   - Waits for the configured delay
   - Retries the operation
3. If it fails with a non-environmental error:
   - Immediately throws `XNavigationError` (no retry)
4. If max attempts reached with environmental error:
   - Throws `XEnvironmentalNavigationError`

#### c. Updated Navigation Methods

All navigation methods now use the retry logic:
- `navigateToUrl(URL url)`
- `navigateBack()`
- `navigateForward()`
- `refreshCurrentPage()`

### 4. Comprehensive Test Suite

**Location**: `webjourney/src/test/java/io/github/jamoamo/webjourney/reserved/selenium/NavigationRetryTest.java`

**Test Coverage**:
- Successful navigation on first attempt
- Environmental error retry success on second attempt
- Environmental error reaching max retries
- Non-environmental errors not being retried
- Retry logic for all navigation methods
- Detection of various environmental error patterns
- Configuration validation

**All 10 tests pass successfully** ✓

## Behavior

### Default Behavior (Automatic Retries)

By default, all navigation operations automatically retry up to 3 times for environmental errors with 1-second delays between retries.

**Example Scenario**:
1. Call `window.navigateToUrl("https://example.com")`
2. First attempt fails with `ERR_CONNECTION_REFUSED`
3. System waits 1 second
4. Second attempt succeeds
5. Navigation completes successfully

### Logging

The retry mechanism provides detailed logging:

**On Retry**:
```
[WARN] Window [MainWindow] navigateToUrl(https://example.com) failed with environmental error on attempt 1: 
       org.openqa.selenium.WebDriverException: unknown error: net::ERR_CONNECTION_REFUSED. Retrying...
```

**On Success After Retry**:
```
[INFO] Window [MainWindow] navigateToUrl(https://example.com) succeeded on attempt 2
```

**On Max Retries Reached**:
```
[ERROR] Window [MainWindow] navigateToUrl(https://example.com) failed after 3 attempts with environmental error
```

### Exception Hierarchy

```
RuntimeException
  └── JourneyException
      └── XWebException
          └── XNavigationError
              └── XEnvironmentalNavigationError (new)
```

## Benefits

1. **Automatic Recovery**: Transient network issues are handled automatically without manual intervention
2. **Clear Error Signaling**: `XEnvironmentalNavigationError` clearly indicates environmental failures
3. **Configurable**: Retry behavior can be customized per window
4. **Smart Detection**: Only environmental errors are retried; logic errors fail fast
5. **Comprehensive**: All navigation methods are protected
6. **Well-Tested**: Full test coverage ensures reliability
7. **Backward Compatible**: Default behavior works with existing code

## Usage Examples

### Basic Usage (Default Settings)

```java
try {
    IWebPage page = window.navigateToUrl(new URL("https://example.com"));
    // Navigation succeeded (possibly after automatic retries)
} catch (XEnvironmentalNavigationError ex) {
    // Environmental error after all retry attempts
    // Consider retrying the entire journey
} catch (XNavigationError ex) {
    // Non-environmental error (not retried)
}
```

### Custom Retry Configuration

```java
// Configure more aggressive retries for unstable networks
NavigationRetryConfig config = new NavigationRetryConfig(5, 2000);
window.setRetryConfig(config);
```

### Journey-Level Retry

```java
public void runJourneyWithRetry(int maxJourneyRetries) {
    int attempt = 0;
    while (attempt < maxJourneyRetries) {
        try {
            // Run journey
            window.navigateToUrl(url);
            // ... journey steps ...
            break; // Success
        } catch (XEnvironmentalNavigationError ex) {
            attempt++;
            if (attempt < maxJourneyRetries) {
                LOGGER.warn("Retrying journey due to environmental error");
                Thread.sleep(5000);
            } else {
                throw ex;
            }
        }
    }
}
```

## Implementation Details

### Thread Safety

The retry mechanism is thread-safe within a single `SeleniumWindow` instance. The retry configuration can be changed, but it's recommended to set it during initialization.

### Performance Considerations

- Default 1-second delay between retries adds minimal overhead
- Retries only occur for environmental errors (rare in stable networks)
- Non-environmental errors fail immediately (no performance impact)

### Future Enhancements

Potential improvements for future releases:
- Configuration via properties/YAML files
- Per-browser retry settings
- Exponential backoff strategy
- Configurable error pattern detection
- Retry metrics and monitoring hooks
- Integration with circuit breaker patterns

## Files Modified/Created

### New Files
- `webjourney/src/main/java/io/github/jamoamo/webjourney/api/web/XEnvironmentalNavigationError.java`
- `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/NavigationRetryConfig.java`
- `webjourney/src/test/java/io/github/jamoamo/webjourney/reserved/selenium/NavigationRetryTest.java`
- `docs/examples/navigation-retry-example.md`
- `docs/NAVIGATION_RETRY_SOLUTION.md` (this file)

### Modified Files
- `webjourney/src/main/java/io/github/jamoamo/webjourney/reserved/selenium/SeleniumWindow.java`
  - Added retry configuration field
  - Added `setRetryConfig()` method
  - Added `isEnvironmentalError()` method
  - Added `executeWithRetry()` method
  - Added `NavigationOperation` functional interface
  - Updated `navigateToUrl()`, `navigateBack()`, `navigateForward()`, and `refreshCurrentPage()` to use retry logic

## Testing

Run the test suite:
```bash
cd webjourney
mvn test -Dtest=NavigationRetryTest
```

All tests pass with 100% success rate.

## Conclusion

This solution provides robust handling of environmental navigation errors through automatic retries and clear error signaling. It's backward compatible, well-tested, and provides the flexibility for consuming applications to implement their own journey-level retry strategies when needed.


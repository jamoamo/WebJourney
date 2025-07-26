# WebJourney Technical Architecture Plan

## Current Architecture Overview

WebJourney follows a clean, modular architecture with the following key components:

```
webjourney-parent/
├── webjourney/           # Core library
│   ├── api/             # Public interfaces
│   ├── reserved/        # Internal implementation
│   └── annotation/      # Annotations for mapping
└── webjourney-test/     # Test utilities
```

## Proposed Architecture Enhancements

### 1. Async and Parallel Execution Framework

#### Current State
- Synchronous journey execution only
- Single browser session per journey
- Blocking operations throughout

#### Proposed Enhancement

```java
// New async journey execution API
public interface AsyncJourneyExecutor {
    CompletableFuture<JourneyResult> executeJourneyAsync(IJourney journey);
    CompletableFuture<List<JourneyResult>> executeJourneysParallel(List<IJourney> journeys);
    CompletableFuture<JourneyResult> executeWithTimeout(IJourney journey, Duration timeout);
}

// Enhanced context for async operations
public interface AsyncJourneyContext extends IJourneyContext {
    CompletableFuture<ActionResult> executeActionAsync(AWebAction action);
    BrowserPool getBrowserPool();
    ExecutorService getActionExecutor();
}
```

#### Implementation Plan
1. **Browser Pool Management**
   ```java
   public class BrowserPool {
       private final Queue<IBrowser> availableBrowsers;
       private final Set<IBrowser> activeBrowsers;
       private final BrowserFactory browserFactory;
       private final int maxPoolSize;
       
       public CompletableFuture<IBrowser> acquireBrowser() { /* */ }
       public void releaseBrowser(IBrowser browser) { /* */ }
   }
   ```

2. **Async Action Framework**
   ```java
   public abstract class AsyncWebAction extends AWebAction {
       public abstract CompletableFuture<ActionResult> executeAsync(AsyncJourneyContext context);
       
       // Default implementation for backward compatibility
       @Override
       public ActionResult execute(IJourneyContext context) {
           return executeAsync((AsyncJourneyContext) context).join();
       }
   }
   ```

### 2. Enhanced Error Handling and Resilience

#### Circuit Breaker Pattern Implementation

```java
public class JourneyCircuitBreaker {
    private final CircuitBreakerConfig config;
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private int failureCount = 0;
    private long lastFailureTime = 0;
    
    public <T> T execute(Supplier<T> operation) throws JourneyException {
        if (state == CircuitBreakerState.OPEN && !shouldAttemptReset()) {
            throw new CircuitBreakerOpenException();
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
}
```

#### Retry Mechanism with Exponential Backoff

```java
public class RetryableJourneyAction {
    private final RetryPolicy retryPolicy;
    
    public ActionResult executeWithRetry(AWebAction action, IJourneyContext context) {
        return Failsafe.with(retryPolicy)
            .onRetry(this::logRetryAttempt)
            .onFailure(this::handleFinalFailure)
            .get(() -> action.execute(context));
    }
}

// Configuration
public class RetryPolicy {
    private int maxAttempts = 3;
    private Duration initialDelay = Duration.ofSeconds(1);
    private double backoffMultiplier = 2.0;
    private Set<Class<? extends Exception>> retryableExceptions;
}
```

### 3. Advanced Browser Management

#### Multi-Browser Support Enhancement

```java
public interface BrowserProvider {
    IBrowser createBrowser(BrowserType type, BrowserOptions options);
    boolean isSupported(BrowserType type);
    BrowserCapabilities getDefaultCapabilities(BrowserType type);
}

public enum BrowserType {
    CHROME, FIREFOX, EDGE, SAFARI, CHROME_MOBILE, FIREFOX_MOBILE
}

// Enhanced browser options
public class EnhancedBrowserOptions extends BrowserOptions {
    private boolean headless = false;
    private boolean incognito = false;
    private Dimension windowSize;
    private Map<String, Object> preferences;
    private List<String> arguments;
    private Duration pageLoadTimeout;
    private Duration implicitWait;
    
    public static EnhancedBrowserOptions chrome() {
        return new EnhancedBrowserOptions(BrowserType.CHROME);
    }
    
    public EnhancedBrowserOptions headless() {
        this.headless = true;
        return this;
    }
}
```

#### WebDriver Manager Integration

```java
public class ManagedWebDriverFactory implements WebDriverFactory {
    
    public WebDriver createDriver(BrowserType type, BrowserOptions options) {
        // Automatically download and manage driver binaries
        switch (type) {
            case CHROME:
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver(buildChromeOptions(options));
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver(buildFirefoxOptions(options));
            // ... other browsers
        }
    }
}
```

### 4. Monitoring and Observability

#### Metrics Collection Framework

```java
public class JourneyMetrics {
    private final MeterRegistry meterRegistry;
    private final Timer journeyExecutionTimer;
    private final Counter actionSuccessCounter;
    private final Counter actionFailureCounter;
    private final Gauge activeBrowsersGauge;
    
    public void recordJourneyExecution(String journeyName, Duration duration) {
        journeyExecutionTimer.record(duration);
    }
    
    public void recordActionResult(String actionType, boolean success) {
        if (success) {
            actionSuccessCounter.increment(Tags.of("action", actionType));
        } else {
            actionFailureCounter.increment(Tags.of("action", actionType));
        }
    }
}
```

#### Distributed Tracing Integration

```java
public class TracingJourneyInterceptor implements JourneyInterceptor {
    private final Tracer tracer;
    
    @Override
    public ActionResult intercept(AWebAction action, IJourneyContext context) {
        Span span = tracer.nextSpan()
            .name("webjourney.action")
            .tag("action.type", action.getClass().getSimpleName())
            .start();
            
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return action.execute(context);
        } catch (Exception e) {
            span.tag("error", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### 5. Enhanced Action Types

#### File Upload/Download Actions

```java
public class FileUploadAction extends AWebAction {
    private final String fileInputSelector;
    private final Path filePath;
    private final boolean waitForCompletion;
    
    @Override
    public ActionResult execute(IJourneyContext context) {
        WebElement fileInput = context.getBrowser().findElement(fileInputSelector);
        fileInput.sendKeys(filePath.toAbsolutePath().toString());
        
        if (waitForCompletion) {
            // Wait for upload completion indicators
            waitForUploadCompletion(context);
        }
        
        return ActionResult.success();
    }
}

public class FileDownloadAction extends AWebAction {
    private final String downloadLinkSelector;
    private final Path downloadDirectory;
    private final Duration timeout;
    
    // Implementation with download monitoring
}
```

#### JavaScript Execution Actions

```java
public class JavaScriptAction extends AWebAction {
    private final String script;
    private final Object[] arguments;
    private final boolean async;
    
    @Override
    public ActionResult execute(IJourneyContext context) {
        JavascriptExecutor js = (JavascriptExecutor) context.getBrowser().getWebDriver();
        
        Object result = async ? 
            js.executeAsyncScript(script, arguments) :
            js.executeScript(script, arguments);
            
        return ActionResult.success(result);
    }
}
```

#### Advanced Wait Strategies

```java
public class CustomWaitAction extends AWebAction {
    private final WaitCondition condition;
    private final Duration timeout;
    private final Duration pollInterval;
    
    @Override
    public ActionResult execute(IJourneyContext context) {
        WebDriverWait wait = new WebDriverWait(
            context.getBrowser().getWebDriver(), 
            timeout
        );
        
        wait.pollingEvery(pollInterval)
            .until(driver -> condition.isMet(driver));
            
        return ActionResult.success();
    }
}

// Custom wait conditions
public interface WaitCondition {
    boolean isMet(WebDriver driver);
    
    static WaitCondition elementVisible(String selector) {
        return driver -> driver.findElement(By.cssSelector(selector)).isDisplayed();
    }
    
    static WaitCondition urlContains(String fragment) {
        return driver -> driver.getCurrentUrl().contains(fragment);
    }
    
    static WaitCondition customScript(String script) {
        return driver -> (Boolean) ((JavascriptExecutor) driver).executeScript(script);
    }
}
```

### 6. Configuration-Driven Journeys

#### YAML Journey Configuration

```yaml
# journey-config.yaml
journey:
  name: "LoginAndCheckout"
  timeout: "5m"
  retryPolicy:
    maxAttempts: 3
    backoffMultiplier: 2.0
  
  steps:
    - type: "navigate"
      url: "https://example.com/login"
      
    - type: "completeForm"
      selector: "#loginForm"
      data:
        username: "${env.TEST_USERNAME}"
        password: "${env.TEST_PASSWORD}"
        
    - type: "clickButton"
      selector: "#loginButton"
      waitFor: ".dashboard"
      
    - type: "conditional"
      condition: "elementExists(.error-message)"
      onTrue:
        - type: "screenshot"
          filename: "login-error-${timestamp}.png"
        - type: "fail"
          message: "Login failed"
```

#### Configuration Parser and Executor

```java
public class ConfigurableJourneyExecutor {
    private final YamlMapper yamlMapper;
    private final ActionFactory actionFactory;
    private final VariableResolver variableResolver;
    
    public IJourney parseJourney(Path configFile) throws IOException {
        JourneyConfig config = yamlMapper.readValue(configFile.toFile(), JourneyConfig.class);
        return buildJourney(config);
    }
    
    private IJourney buildJourney(JourneyConfig config) {
        JourneyBuilder builder = JourneyBuilder.start();
        
        for (StepConfig step : config.getSteps()) {
            AWebAction action = actionFactory.createAction(step);
            builder.then(action);
        }
        
        return builder.build();
    }
}
```

### 7. Integration Framework

#### Spring Boot Starter

```java
@Configuration
@EnableConfigurationProperties(WebJourneyProperties.class)
public class WebJourneyAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public WebTraveller webTraveller(WebJourneyProperties properties) {
        return new WebTraveller(createTravelOptions(properties));
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "webjourney", name = "async.enabled", havingValue = "true")
    public AsyncJourneyExecutor asyncJourneyExecutor() {
        return new DefaultAsyncJourneyExecutor();
    }
    
    @Bean
    @ConditionalOnProperty(prefix = "webjourney.metrics", name = "enabled", havingValue = "true")
    public JourneyMetrics journeyMetrics(MeterRegistry meterRegistry) {
        return new JourneyMetrics(meterRegistry);
    }
}

@ConfigurationProperties(prefix = "webjourney")
public class WebJourneyProperties {
    private Browser browser = new Browser();
    private Async async = new Async();
    private Metrics metrics = new Metrics();
    
    // Nested configuration classes
}
```

#### TestNG Integration

```java
public class WebJourneyTestNG extends AbstractTestNGSpringContextTests {
    
    @Autowired
    private WebTraveller webTraveller;
    
    @Test(dataProvider = "journeyProvider")
    public void executeJourney(IJourney journey) {
        webTraveller.travelJourney(journey);
    }
    
    @DataProvider
    public Object[][] journeyProvider() {
        return JourneyDataProvider.loadJourneys("test-journeys");
    }
}
```

### 8. Performance Optimizations

#### Lazy Element Loading

```java
public class LazyWebElement implements WebElement {
    private final Supplier<WebElement> elementSupplier;
    private WebElement cachedElement;
    private final Duration cacheTimeout;
    private Instant lastAccessed;
    
    @Override
    public void click() {
        getElement().click();
    }
    
    private WebElement getElement() {
        if (cachedElement == null || shouldRefresh()) {
            cachedElement = elementSupplier.get();
            lastAccessed = Instant.now();
        }
        return cachedElement;
    }
}
```

#### Connection Pooling for Remote WebDriver

```java
public class RemoteWebDriverPool {
    private final Queue<RemoteWebDriver> pool;
    private final String hubUrl;
    private final DesiredCapabilities capabilities;
    private final int maxPoolSize;
    
    public RemoteWebDriver borrowDriver() {
        RemoteWebDriver driver = pool.poll();
        if (driver == null || !isDriverHealthy(driver)) {
            driver = createNewDriver();
        }
        return driver;
    }
    
    public void returnDriver(RemoteWebDriver driver) {
        if (isDriverHealthy(driver) && pool.size() < maxPoolSize) {
            pool.offer(driver);
        } else {
            driver.quit();
        }
    }
}
```

## Implementation Strategy

### Phase 1: Foundation (Weeks 1-4)
1. **Async Framework Setup**
   - Implement basic CompletableFuture support
   - Create browser pool infrastructure
   - Add async action base classes

2. **Enhanced Configuration**
   - YAML/JSON configuration support
   - Environment variable resolution
   - Configuration validation

### Phase 2: Resilience (Weeks 5-8)
1. **Error Handling Enhancement**
   - Circuit breaker implementation
   - Retry mechanisms with backoff
   - Enhanced exception hierarchy

2. **Monitoring Integration**
   - Metrics collection framework
   - Basic distributed tracing
   - Health check endpoints

### Phase 3: Advanced Features (Weeks 9-12)
1. **Enhanced Actions**
   - File upload/download support
   - JavaScript execution actions
   - Advanced wait strategies

2. **Browser Management**
   - Multi-browser support
   - WebDriver manager integration
   - Mobile browser capabilities

### Phase 4: Integration (Weeks 13-16)
1. **Framework Integration**
   - Spring Boot starter
   - TestNG integration
   - Cucumber BDD support

2. **Performance Optimization**
   - Lazy loading implementation
   - Connection pooling
   - Memory optimization

## Testing Strategy

### Unit Testing
- Mock-based testing for individual components
- Property-based testing for complex logic
- Mutation testing for test quality assurance

### Integration Testing
- TestContainers for isolated browser testing
- Real browser compatibility testing
- Performance regression testing

### End-to-End Testing
- Full journey execution tests
- Multi-browser compatibility matrix
- Load testing with concurrent journeys

## Migration Strategy

### Backward Compatibility
- Maintain existing API surface
- Deprecate old methods gradually
- Provide migration guides and tools

### Version Strategy
- Semantic versioning with clear breaking change indicators
- Beta releases for early feedback
- Long-term support for stable versions

This technical architecture plan provides a comprehensive roadmap for implementing the proposed enhancements while maintaining backward compatibility and ensuring robust, scalable solutions. 
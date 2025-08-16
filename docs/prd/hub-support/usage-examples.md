# Hub Support Usage Examples

## Basic Usage Examples

### 1. Simple Hub Configuration

#### Programmatic Configuration
```java
import io.github.jamoamo.webjourney.*;
import io.github.jamoamo.webjourney.api.web.*;
import io.github.jamoamo.webjourney.reserved.selenium.RemoteChromeBrowserFactory;

public class BasicHubExample {
    public void simpleHubUsage() {
        // Create hub configuration
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withConnectionTimeout(Duration.ofSeconds(30))
            .withSessionTimeout(Duration.ofMinutes(10))
            .build();
        
        // Create remote browser factory
        IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(hubConfig);
        
        // Create travel options with remote factory
        ITravelOptions travelOptions = new TravelOptions(
            new PreferredBrowserStrategy(remoteChromeFactory)
        );
        
        // Use as normal
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

#### Environment Variable Configuration
```bash
# Set environment variables
export WEBJOURNEY_HUB_ENABLED=true
export WEBJOURNEY_HUB_URL=http://selenium-hub:4444/wd/hub
export WEBJOURNEY_HUB_CONNECTION_TIMEOUT=30s
export WEBJOURNEY_HUB_SESSION_TIMEOUT=10m
```

```java
public class EnvironmentBasedHubExample {
    public void environmentConfiguration() {
        // Configuration automatically loaded from environment
        AsyncConfiguration config = AsyncConfiguration.fromEnvironment(System::getenv);
        
        // Create remote factory with environment config
        IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(
            config.getHubConfiguration()
        );
        
        ITravelOptions travelOptions = new TravelOptions(
            new PreferredBrowserStrategy(remoteChromeFactory)
        );
        
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 2. Multi-Browser Hub Support

```java
public class MultiBrowserHubExample {
    public void multiBrowserSetup() {
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withConnectionTimeout(Duration.ofSeconds(30))
            .build();
        
        // Create remote factories for all browsers
        IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(hubConfig);
        IBrowserFactory remoteFirefoxFactory = new RemoteFirefoxBrowserFactory(hubConfig);
        IBrowserFactory remoteEdgeFactory = new RemoteEdgeBrowserFactory(hubConfig);
        
        // Priority browser strategy with multiple remote browsers
        ITravelOptions travelOptions = new TravelOptions(
            new PriorityBrowserStrategy(new IBrowserFactory[] {
                remoteChromeFactory,
                remoteFirefoxFactory,
                remoteEdgeFactory
            })
        );
        
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

## Advanced Usage Examples

### 3. Hub-Aware Strategy with Fallback

```java
public class FallbackStrategyExample {
    public void hubAwareWithFallback() {
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withConnectionTimeout(Duration.ofSeconds(10))
            .withMaxRetries(2)
            .build();
        
        // Hub-aware strategy that falls back to local execution
        IPreferredBrowserStrategy strategy = new HubAwareBrowserStrategy()
            .withRemoteFactory(new RemoteChromeBrowserFactory(hubConfig))
            .withLocalFactory(new ChromeBrowserFactory())
            .withFallback(true)
            .withHealthMonitoring(true);
        
        ITravelOptions travelOptions = new TravelOptions(strategy);
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        // Will attempt hub connection, fall back to local on failure
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 4. Load Balancing Across Multiple Hubs

```java
public class LoadBalancingExample {
    public void loadBalancedHubs() {
        // Load balancing strategy across multiple hubs
        IPreferredBrowserStrategy strategy = new LoadBalancingBrowserStrategy()
            .addHub("http://hub1:4444/wd/hub", 1.0)     // Primary hub - full weight
            .addHub("http://hub2:4444/wd/hub", 0.7)     // Secondary hub - 70% weight
            .addHub("http://hub3:4444/wd/hub", 0.3)     // Tertiary hub - 30% weight
            .withLoadBalancer(LoadBalancer.WEIGHTED_ROUND_ROBIN)
            .withHealthMonitoring(Duration.ofSeconds(30))
            .withLocalFallback(true);
        
        ITravelOptions travelOptions = new TravelOptions(strategy);
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        // Requests will be distributed across available hubs
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 5. Custom Capabilities and Node Selection

```java
public class AdvancedCapabilitiesExample {
    public void advancedNodeSelection() {
        // Advanced Grid node selection
        GridNodeSelector nodeSelector = new GridNodeSelector()
            .requirePlatform("LINUX")
            .requireBrowserVersion("latest")
            .requireCustomCapability("selenium:node-name", "high-performance-node")
            .preferNode("dedicated-test-node");
        
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withNodeSelector(nodeSelector)
            .withCustomCapability("enableVNC", true)
            .withCustomCapability("enableVideo", true)
            .withCustomCapability("videoName", "test-recording.mp4")
            .build();
        
        IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(hubConfig);
        
        ITravelOptions travelOptions = new TravelOptions(
            new PreferredBrowserStrategy(remoteChromeFactory)
        );
        
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 6. Per-Journey Hub Configuration

```java
public class PerJourneyConfigExample {
    public void perJourneyConfiguration() {
        // Base travel options with local execution
        ITravelOptions baseTravelOptions = new TravelOptions(
            new PreferredBrowserStrategy(StandardBrowser.CHROME)
        );
        
        WebTraveller traveller = new WebTraveller(baseTravelOptions);
        
        // Journey 1: Run locally
        traveller.travelJourney(new LocalTestJourney());
        
        // Journey 2: Run on hub with specific configuration
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://performance-hub:4444/wd/hub")
            .withCustomCapability("performance-testing", true)
            .build();
        
        IJourney hubJourney = new JourneyBuilder()
            .withHubConfiguration(hubConfig)
            .navigate("https://example.com")
            .consumePage(new ExamplePageConsumer())
            .build();
        
        traveller.travelJourney(hubJourney);
    }
}
```

## Configuration File Examples

### 7. YAML Configuration

```yaml
# webjourney-config.yml
webjourney:
  hub:
    enabled: true
    url: ${SELENIUM_HUB_URL:http://localhost:4444/wd/hub}
    connectionTimeout: 30s
    sessionTimeout: 10m
    maxRetries: 3
    retryDelay: 2s
    
    customCapabilities:
      enableVNC: true
      enableVideo: false
      timeZone: "America/New_York"
      
    nodeSelection:
      platform: LINUX
      browserVersion: latest
      customCapabilities:
        "selenium:node-type": "standard"
        
  browserArguments:
    chrome:
      - "--window-size=1920,1080"
      - "--disable-gpu"
    firefox:
      - "--width=1920"
      - "--height=1080"
```

```java
public class YamlConfigExample {
    public void yamlBasedConfiguration() {
        // Load configuration from YAML file
        AsyncConfiguration config = AsyncConfiguration.fromYaml("webjourney-config.yml");
        
        IBrowserFactory remoteChromeFactory = new RemoteChromeBrowserFactory(
            config.getHubConfiguration(),
            config,
            new DefaultBrowserArgumentsProvider(System::getenv, config)
        );
        
        ITravelOptions travelOptions = new TravelOptions(
            new PreferredBrowserStrategy(remoteChromeFactory)
        );
        
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 8. Properties Configuration

```properties
# webjourney.properties
webjourney.hub.enabled=true
webjourney.hub.url=http://selenium-hub:4444/wd/hub
webjourney.hub.connectionTimeout=30s
webjourney.hub.sessionTimeout=10m
webjourney.hub.maxRetries=3

webjourney.hub.customCapabilities.enableVNC=true
webjourney.hub.customCapabilities.enableVideo=false
webjourney.hub.customCapabilities.timeZone=America/New_York

webjourney.hub.nodeSelection.platform=LINUX
webjourney.hub.nodeSelection.browserVersion=latest
```

## Docker and Container Examples

### 9. Docker Compose Integration

```yaml
# docker-compose.yml
version: '3.8'
services:
  test-app:
    build: .
    environment:
      - WEBJOURNEY_HUB_ENABLED=true
      - WEBJOURNEY_HUB_URL=http://selenium-hub:4444/wd/hub
      - WEBJOURNEY_HUB_CONNECTION_TIMEOUT=30s
      - WEBJOURNEY_HUB_SESSION_TIMEOUT=10m
    depends_on:
      selenium-hub:
        condition: service_healthy
    networks:
      - selenium-grid
      
  selenium-hub:
    image: selenium/hub:latest
    ports:
      - "4444:4444"
    environment:
      - GRID_MAX_SESSION=16
      - GRID_BROWSER_TIMEOUT=300
      - GRID_TIMEOUT=300
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:4444/wd/hub/status"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - selenium-grid
      
  chrome-node:
    image: selenium/node-chrome:latest
    environment:
      - HUB_HOST=selenium-hub
      - NODE_MAX_INSTANCES=4
      - NODE_MAX_SESSION=4
    depends_on:
      selenium-hub:
        condition: service_healthy
    volumes:
      - /dev/shm:/dev/shm
    shm_size: 2gb
    networks:
      - selenium-grid

networks:
  selenium-grid:
    driver: bridge
```

```java
public class DockerComposeExample {
    public void dockerComposeSetup() {
        // Configuration automatically picks up environment variables
        // set by Docker Compose
        AsyncConfiguration config = AsyncConfiguration.fromEnvironment(System::getenv);
        
        if (config.isHubEnabled()) {
            IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(
                config.getHubConfiguration()
            );
            
            ITravelOptions travelOptions = new TravelOptions(
                new PreferredBrowserStrategy(remoteFactory)
            );
            
            WebTraveller traveller = new WebTraveller(travelOptions);
            traveller.travelJourney(new MyTestJourney());
        }
    }
}
```

### 10. Kubernetes Deployment

```yaml
# kubernetes-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: webjourney-tests
spec:
  replicas: 3
  selector:
    matchLabels:
      app: webjourney-tests
  template:
    metadata:
      labels:
        app: webjourney-tests
    spec:
      containers:
      - name: test-runner
        image: my-webjourney-tests:latest
        env:
        - name: WEBJOURNEY_HUB_ENABLED
          value: "true"
        - name: WEBJOURNEY_HUB_URL
          value: "http://selenium-hub-service:4444/wd/hub"
        - name: WEBJOURNEY_HUB_CONNECTION_TIMEOUT
          value: "60s"
        - name: WEBJOURNEY_HUB_SESSION_TIMEOUT
          value: "15m"
        - name: WEBJOURNEY_HUB_MAX_RETRIES
          value: "5"
```

## Enterprise Integration Examples

### 11. CI/CD Pipeline Integration

```yaml
# .github/workflows/tests.yml
name: WebJourney Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        browser: [chrome, firefox, edge]
    
    services:
      selenium-hub:
        image: selenium/hub:latest
        ports:
          - 4444:4444
      
      selenium-chrome:
        image: selenium/node-chrome:latest
        env:
          HUB_HOST: selenium-hub
      
      selenium-firefox:
        image: selenium/node-firefox:latest
        env:
          HUB_HOST: selenium-hub
      
      selenium-edge:
        image: selenium/node-edge:latest
        env:
          HUB_HOST: selenium-hub
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests
      env:
        WEBJOURNEY_HUB_ENABLED: true
        WEBJOURNEY_HUB_URL: http://localhost:4444/wd/hub
        WEBJOURNEY_BROWSER: ${{ matrix.browser }}
      run: ./mvnw test
```

### 12. Enterprise Hub with Authentication

```java
public class EnterpriseHubExample {
    public void enterpriseHubWithAuth() {
        // Custom authentication provider
        GridAuthenticationProvider authProvider = new BasicAuthProvider(
            "hub-username", 
            "hub-password"
        );
        
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("https://enterprise-hub.company.com:4444/wd/hub")
            .withAuthenticationProvider(authProvider)
            .withConnectionTimeout(Duration.ofMinutes(2))
            .withSessionTimeout(Duration.ofMinutes(30))
            .withCustomCapability("company:project", "webjourney-tests")
            .withCustomCapability("company:team", "qa-automation")
            .build();
        
        IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
        
        ITravelOptions travelOptions = new TravelOptions(
            new PreferredBrowserStrategy(remoteFactory)
        );
        
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

### 13. Monitoring and Observability

```java
public class MonitoringExample {
    public void monitoringAndMetrics() {
        // Configure metrics collection
        GridMetrics metrics = new GridMetrics()
            .withMeterRegistry(Metrics.globalRegistry)
            .withTags("service", "webjourney", "environment", "production");
        
        // Configure distributed tracing
        GridTracing tracing = new GridTracing()
            .withTracer(GlobalOpenTelemetry.getTracer("webjourney-hub"));
        
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withMetrics(metrics)
            .withTracing(tracing)
            .build();
        
        IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
        
        // Health monitoring with alerts
        IGridHealthMonitor healthMonitor = new GridHealthMonitor()
            .withMonitoringInterval(Duration.ofSeconds(30))
            .withHealthListener(new AlertingHealthListener("ops-team@company.com"));
        
        IPreferredBrowserStrategy strategy = new HubAwareBrowserStrategy()
            .withRemoteFactory(remoteFactory)
            .withHealthMonitor(healthMonitor);
        
        ITravelOptions travelOptions = new TravelOptions(strategy);
        WebTraveller traveller = new WebTraveller(travelOptions);
        traveller.travelJourney(new MyTestJourney());
    }
}
```

## Error Handling and Troubleshooting Examples

### 14. Robust Error Handling

```java
public class ErrorHandlingExample {
    public void robustErrorHandling() {
        IHubConfiguration hubConfig = new HubConfiguration.Builder()
            .withUrl("http://selenium-hub:4444/wd/hub")
            .withConnectionTimeout(Duration.ofSeconds(30))
            .withMaxRetries(3)
            .withRetryDelay(Duration.ofSeconds(5))
            .build();
        
        IBrowserFactory remoteFactory = new RemoteChromeBrowserFactory(hubConfig);
        IBrowserFactory localFactory = new ChromeBrowserFactory();
        
        IPreferredBrowserStrategy strategy = new FallbackBrowserStrategy()
            .addFactory(remoteFactory)
            .addFactory(localFactory)
            .withPolicy(FallbackPolicy.TRY_ALL_WITH_LOCAL);
        
        ITravelOptions travelOptions = new TravelOptions(strategy);
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        try {
            traveller.travelJourney(new MyTestJourney());
        } catch (RemoteBrowserException e) {
            // Log detailed error information
            LOGGER.error("Remote browser creation failed: {}", e.getMessage());
            LOGGER.error("Hub URL: {}", e.getHubUrl());
            LOGGER.error("Capabilities: {}", e.getCapabilities());
            LOGGER.error("Retry attempts: {}", e.getRetryAttempts());
            
            // Attempt manual recovery or notification
            notifyOpsTeam("Hub connectivity issues detected", e);
            
            // Re-throw or handle as appropriate
            throw e;
        }
    }
}
```

These examples demonstrate the full range of hub support capabilities, from basic usage to enterprise-grade deployments with monitoring, authentication, and robust error handling. Each example can be adapted to specific use cases and environments.

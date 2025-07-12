# WebJourney Performance Testing

This directory contains JMeter performance tests for the WebJourney library.

## Current Status

⚠️ **IMPORTANT**: The JMeter tests are currently **disabled** in the Maven build because they require a running test server but execute standalone without proper test server setup.

## The Problem

The JMeter tests expect a web server running at `http://localhost:80` (or configured port) serving the test HTML files from `src/test/resources/integration-test-sites/`, but no such server is started when the JMeter plugin executes.

This causes:
- 99.99% failure rate in performance tests
- Build failures due to connection refused errors
- Inability to test actual WebJourney functionality

## Working Alternative

The **integration tests** in `src/test/java/.../integration/PerformanceTestIT.java` work correctly because they:
1. Use `WebJourneyTestContainerBase` to start an Nginx container with TestContainers
2. Serve the test HTML files automatically
3. Run actual WebJourney performance tests with proper infrastructure

## Test Structure

```
performance-tests/
├── browser-automation-tests/
│   └── journey-execution-load.jmx     # WebJourney browser automation load test
└── load-tests/
    ├── basic-load-test.jmx            # Basic HTTP load test
    └── stress-test.jmx                # Stress testing scenario
```

## How to Run Performance Tests

### Option 1: Use Integration Tests (Recommended)
```bash
mvn test -Dtest=PerformanceTestIT
```

### Option 2: Run JMeter Tests Manually
1. Start a test server:
   ```bash
   # Using Python (simple approach)
   cd webjourney/src/test/resources/integration-test-sites
   python -m http.server 80
   
   # Or using Docker
   docker run -p 80:80 -v $(pwd)/webjourney/src/test/resources/integration-test-sites:/usr/share/nginx/html nginx:alpine
   ```

2. Run JMeter tests:
   ```bash
   mvn jmeter:jmeter -Dwebjourney.test.baseurl=http://localhost:80
   ```

## Fixing the JMeter Integration

To properly integrate JMeter tests with the build, you need to:

1. **Add a test server setup phase** before JMeter execution
2. **Configure the base URL** to point to the running test server
3. **Remove the `<skip>true</skip>` configuration** in `pom.xml`

Example using TestContainers in a Maven plugin:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-antrun-plugin</artifactId>
    <executions>
        <execution>
            <id>start-test-server</id>
            <phase>pre-integration-test</phase>
            <goals>
                <goal>run</goal>
            </goals>
            <configuration>
                <tasks>
                    <!-- Start test server here -->
                </tasks>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Test Configuration

The JMeter tests are configured with the following properties:
- `webjourney.test.baseurl`: Base URL for test server (default: http://localhost:80)
- `webjourney.threads`: Number of threads for load tests (default: 10)
- `webjourney.rampup`: Ramp-up time in seconds (default: 60)
- `webjourney.duration`: Test duration in seconds (default: 300)
- `webjourney.stress.threads`: Stress test threads (default: 50)
- `webjourney.browser.threads`: Browser automation threads (default: 5)

## Test Scenarios

1. **Basic Load Test**: Tests HTTP endpoints with multiple threads
2. **Stress Test**: High-load scenario with many concurrent users
3. **Browser Automation Test**: Tests actual WebJourney browser automation performance 
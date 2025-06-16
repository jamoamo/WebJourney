# Installation Guide

This guide covers everything you need to install and configure WebJourney in your project.

## System Requirements

### Java Version
- **Java 21 or higher** is required
- Verify your Java version: `java -version`

### Build Tools
- **Maven 3.6+** or **Gradle 7+**
- Verify Maven version: `mvn -version`
- Verify Gradle version: `gradle -version`

### Supported Browsers
WebJourney supports all major browsers:
- **Chrome** (recommended)
- **Firefox**
- **Microsoft Edge**
- **Safari** (macOS only)

## Adding WebJourney to Your Project

### Maven Projects

Add to your `pom.xml`:

```xml
<dependencies>
    <!-- Core WebJourney library -->
    <dependency>
        <groupId>io.github.jamoamo</groupId>
        <artifactId>webjourney</artifactId>
        <version>3.3.1-SNAPSHOT</version>
    </dependency>
    
    <!-- Optional: Test utilities for unit testing -->
    <dependency>
        <groupId>io.github.jamoamo</groupId>
        <artifactId>webjourney-test</artifactId>
        <version>3.3.1-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Recommended: SLF4J implementation for logging -->
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j2-impl</artifactId>
        <version>2.24.3</version>
    </dependency>
</dependencies>
```

### Gradle Projects

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.jamoamo:webjourney:3.3.1-SNAPSHOT'
    
    // Optional: Test utilities
    testImplementation 'io.github.jamoamo:webjourney-test:3.3.1-SNAPSHOT'
    
    // Recommended: SLF4J implementation for logging
    implementation 'org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3'
}
```

## Browser Driver Setup

WebJourney uses Selenium WebDriver, which requires browser-specific drivers. Here are your options:

### Option 1: Automatic Driver Management (Recommended)

WebJourney automatically manages drivers for supported browsers. No additional setup required!

```java
// WebJourney will automatically download and manage Chrome driver
TravelOptions options = TravelOptions.builder()
    .withBrowser(BrowserType.CHROME)
    .build();
```

### Option 2: Manual Driver Installation

If you prefer to manage drivers manually:

#### Chrome
1. Download ChromeDriver from [chromedriver.chromium.org](https://chromedriver.chromium.org/)
2. Add to your system PATH or specify the location:

```java
System.setProperty("webdriver.chrome.driver", "/path/to/chromedriver");
```

#### Firefox
1. Download GeckoDriver from [GitHub releases](https://github.com/mozilla/geckodriver/releases)
2. Add to your system PATH or specify the location:

```java
System.setProperty("webdriver.gecko.driver", "/path/to/geckodriver");
```

#### Microsoft Edge
1. Download EdgeDriver from [Microsoft Edge Developer](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/)
2. Add to your system PATH or specify the location:

```java
System.setProperty("webdriver.edge.driver", "/path/to/msedgedriver");
```

## Configuration

### Basic Configuration

Create a basic configuration for your journeys:

```java
import io.github.jamoamo.webjourney.*;

public class WebJourneyConfig {
    public static TravelOptions createDefaultOptions() {
        return TravelOptions.builder()
            .withBrowser(BrowserType.CHROME)
            .withHeadless(true)  // Run without UI
            .withTimeout(Duration.ofSeconds(30))
            .build();
    }
}
```

### Advanced Configuration

For more control over browser behavior:

```java
import io.github.jamoamo.webjourney.*;
import io.github.jamoamo.webjourney.api.web.*;

public class AdvancedConfig {
    public static TravelOptions createCustomOptions() {
        return TravelOptions.builder()
            .withBrowser(BrowserType.CHROME)
            .withHeadless(false)  // Show browser window
            .withTimeout(Duration.ofMinutes(2))
            .withImplicitWait(Duration.ofSeconds(10))
            .withPageLoadTimeout(Duration.ofSeconds(60))
            .withWindowSize(1920, 1080)
            .withBrowserOptions(chromeOptions -> {
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
            })
            .build();
    }
}
```

## Logging Configuration

WebJourney uses SLF4J for logging. Create a `log4j2.xml` file in your `src/main/resources`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <!-- WebJourney logging -->
        <Logger name="io.github.jamoamo.webjourney" level="INFO"/>
        
        <!-- Selenium logging (can be noisy) -->
        <Logger name="org.openqa.selenium" level="WARN"/>
        
        <Root level="INFO">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## Verification

### Quick Test

Create a simple test to verify your installation:

```java
import io.github.jamoamo.webjourney.*;
import io.github.jamoamo.webjourney.api.*;

public class InstallationTest {
    public static void main(String[] args) {
        System.out.println("Testing WebJourney installation...");
        
        TravelOptions options = TravelOptions.builder()
            .withBrowser(BrowserType.CHROME)
            .withHeadless(true)
            .build();
        
        IJourney journey = JourneyBuilder.start()
            .navigateTo("https://example.com")
            .consumePage(context -> {
                String title = context.getBrowser().getTitle();
                System.out.println("Page title: " + title);
                return title;
            })
            .build();
        
        WebTraveller traveller = new WebTraveller(options);
        try {
            traveller.travelJourney(journey);
            System.out.println("✅ WebJourney is working correctly!");
        } catch (Exception e) {
            System.err.println("❌ Installation issue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

Run this test:
- **Maven:** `mvn compile exec:java -Dexec.mainClass="InstallationTest"`
- **Gradle:** `gradle run --main-class=InstallationTest`

### Expected Output

If everything is working correctly, you should see:

```
Testing WebJourney installation...
Page title: Example Domain
✅ WebJourney is working correctly!
```

## Troubleshooting Installation Issues

### Common Problems

#### "WebDriver executable not found"
- **Solution:** Ensure you have the correct browser installed, or use automatic driver management
- **Check:** Browser version compatibility with WebDriver

#### "SessionNotCreatedException"
- **Solution:** Update your browser or WebDriver version
- **Check:** Browser and driver version compatibility

#### "OutOfMemoryError"
- **Solution:** Increase JVM heap size: `-Xmx2g`
- **Check:** Close unused browser instances

#### Permission Issues (Linux/macOS)
- **Solution:** Make driver executable: `chmod +x /path/to/driver`
- **Check:** Driver file permissions

### Getting Help

If you're still having issues:

1. Check the [Common Issues](../troubleshooting/common-issues.md) guide
2. Review [browser compatibility](../troubleshooting/browser-compatibility.md)
3. Ask on [GitHub Discussions](https://github.com/jamoamo/webjourney/discussions)

## Next Steps

With WebJourney installed, you're ready to:

1. **[Create Your First Journey](./first-journey.md)** - Step-by-step tutorial
2. **[Learn Basic Concepts](./concepts.md)** - Understanding core principles
3. **[Explore Examples](../../../examples/basic/)** - Practical code examples

Happy automating! 🚀 
# Getting Started with WebJourney

Welcome to WebJourney! This guide will help you install, configure, and create your first web automation journey.

## What is WebJourney?

WebJourney is a Java library that simplifies web automation by allowing you to define sequences of web actions as "journeys." Built on top of Selenium WebDriver, it provides a fluent, easy-to-use API for common web automation tasks.

### Key Features

- **Fluent API** - Chain actions together naturally
- **Built-in Error Handling** - Robust error recovery and reporting
- **Multiple Browser Support** - Chrome, Firefox, Edge, and more
- **Flexible Configuration** - Headless mode, timeouts, and custom options
- **Page Object Integration** - Extract data from pages efficiently
- **Journey Composition** - Break complex workflows into reusable pieces

## Prerequisites

Before you begin, ensure you have:

- **Java 21 or higher** installed
- **Maven 3.6+** or **Gradle 7+** for dependency management
- A supported web browser (Chrome, Firefox, Edge, or Safari)

## Installation

### Maven

Add WebJourney to your `pom.xml`:

```xml
<dependencies>
    <!-- Core WebJourney library -->
    <dependency>
        <groupId>io.github.jamoamo</groupId>
        <artifactId>webjourney</artifactId>
        <version>3.3.1-SNAPSHOT</version>
    </dependency>
    
    <!-- Optional: Test utilities -->
    <dependency>
        <groupId>io.github.jamoamo</groupId>
        <artifactId>webjourney-test</artifactId>
        <version>3.3.1-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Gradle

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.jamoamo:webjourney:3.3.1-SNAPSHOT'
    
    // Optional: Test utilities
    testImplementation 'io.github.jamoamo:webjourney-test:3.3.1-SNAPSHOT'
}
```

## Your First Journey

Let's create a simple journey that navigates to a website and extracts some information:

```java
import io.github.jamoamo.webjourney.*;
import io.github.jamoamo.webjourney.api.*;

public class FirstJourneyExample {
    public static void main(String[] args) {
        // 1. Configure travel options
        TravelOptions options = TravelOptions.builder()
            .withBrowser(BrowserType.CHROME)
            .withHeadless(true)  // Run without opening browser window
            .withTimeout(Duration.ofSeconds(30))
            .build();
        
        // 2. Create your journey
        IJourney journey = JourneyBuilder.start()
            .navigateTo("https://example.com")
            .waitFor("h1")  // Wait for page to load
            .consumePage(new ExamplePageConsumer())
            .build();
        
        // 3. Execute the journey
        WebTraveller traveller = new WebTraveller(options);
        try {
            traveller.travelJourney(journey);
            System.out.println("Journey completed successfully!");
        } catch (JourneyException e) {
            System.err.println("Journey failed: " + e.getMessage());
        }
    }
}

// Extract data from the page
class ExamplePageConsumer implements IPageConsumer<String> {
    @Override
    public String consumePage(IJourneyContext context) {
        String title = context.getBrowser()
            .findElement("h1")
            .getText();
        System.out.println("Page title: " + title);
        return title;
    }
}
```

## Understanding the Basic Concepts

### 1. TravelOptions
Configure how your journey runs:

```java
TravelOptions options = TravelOptions.builder()
    .withBrowser(BrowserType.CHROME)     // Browser to use
    .withHeadless(true)                  // Run without UI
    .withTimeout(Duration.ofSeconds(30)) // Global timeout
    .build();
```

### 2. Journey Builder
Chain actions together:

```java
IJourney journey = JourneyBuilder.start()
    .navigateTo("https://example.com")           // Navigate to URL
    .clickButton("#login-btn")                   // Click an element
    .completeForm("#login-form", form -> form    // Fill out a form
        .setValue("username", "testuser")
        .setValue("password", "testpass"))
    .waitFor(".dashboard")                       // Wait for element
    .consumePage(DashboardConsumer.class)        // Extract data
    .build();
```

### 3. Web Traveller
Executes your journey:

```java
WebTraveller traveller = new WebTraveller(options);
traveller.travelJourney(journey);
```

## Common Actions

WebJourney provides many built-in actions:

### Navigation
```java
.navigateTo("https://example.com")           // Go to URL
.navigateBack()                              // Browser back
.navigateForward()                           // Browser forward
.refreshPage()                               // Refresh current page
```

### Interactions
```java
.clickButton("#submit-btn")                  // Click element
.clickLink("a[href='/about']")              // Click link
.completeForm("#contact-form", form -> form  // Fill form
    .setValue("name", "John Doe")
    .setValue("email", "john@example.com"))
```

### Waiting
```java
.waitFor(".loading-spinner")                 // Wait for element to appear
.waitFor(".content", Duration.ofSeconds(10)) // Wait with custom timeout
```

### Data Extraction
```java
.consumePage(MyPageConsumer.class)           // Extract data from page
```

## Next Steps

Now that you have WebJourney running, explore these topics:

1. **[Basic Concepts](./concepts.md)** - Deeper dive into journeys, actions, and contexts
2. **[Your First Journey](./first-journey.md)** - Step-by-step tutorial with a real example
3. **[Installation Guide](./installation.md)** - Detailed setup including browser drivers
4. **[Basic Examples](../../../examples/basic/)** - More practical examples

## Getting Help

If you run into issues:

- Check the [Troubleshooting Guide](../troubleshooting/common-issues.md)
- Review the [FAQ](../troubleshooting/faq.md)
- Ask questions in [GitHub Discussions](https://github.com/jamoamo/webjourney/discussions)

Ready to build more complex automations? Check out the [Advanced Guide](../advanced/) next! 
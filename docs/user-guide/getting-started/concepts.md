# WebJourney Core Concepts

Understanding WebJourney's core concepts will help you build more effective and maintainable web automation solutions.

## Overview

WebJourney is built around the metaphor of a "journey" - a sequence of actions that you take on the web. Just like planning a real journey, you define your route (sequence of actions) and then let WebJourney execute it for you.

## Core Components

### 1. Journey

A **Journey** represents a complete workflow or sequence of web actions. It's the highest-level concept in WebJourney.

```java
IJourney journey = JourneyBuilder.start()
    .navigateTo("https://example.com")
    .clickButton("#login")
    .completeForm("#loginForm", form -> form
        .setValue("username", "testuser")
        .setValue("password", "testpass"))
    .waitFor(".dashboard")
    .build();
```

**Key characteristics:**
- **Sequential**: Actions execute in the order you define them
- **Reusable**: Can be executed multiple times
- **Composable**: Can include sub-journeys for complex workflows

### 2. Actions

**Actions** are the individual steps within a journey. Each action performs a specific task on a web page.

#### Navigation Actions
```java
.navigateTo("https://example.com")        // Go to a URL
.navigateBack()                           // Browser back button
.navigateForward()                        // Browser forward button
.refreshPage()                            // Refresh current page
```

#### Interaction Actions
```java
.clickButton("#submit")                   // Click an element
.clickLink("a[href='/about']")           // Click a link
.completeForm("#form", form -> form       // Fill out a form
    .setValue("name", "John")
    .setValue("email", "john@example.com"))
```

#### Wait Actions
```java
.waitFor(".content")                      // Wait for element to appear
.waitFor(".spinner", Duration.ofSeconds(10))  // Wait with timeout
```

#### Data Extraction Actions
```java
.consumePage(PageConsumer.class)          // Extract data from page
.consumePage(context -> {                 // Inline data extraction
    return context.getBrowser().getTitle();
})
```

### 3. Travel Options

**Travel Options** configure how your journey executes - which browser to use, timeouts, and other settings.

```java
TravelOptions options = TravelOptions.builder()
    .withBrowser(BrowserType.CHROME)      // Browser choice
    .withHeadless(true)                   // Run without UI
    .withTimeout(Duration.ofSeconds(30))  // Global timeout
    .withImplicitWait(Duration.ofSeconds(10))  // Element wait time
    .build();
```

**Common configurations:**
- **Browser Type**: Chrome, Firefox, Edge, Safari
- **Headless Mode**: Run without opening browser windows
- **Timeouts**: How long to wait for pages and elements
- **Window Size**: Browser window dimensions
- **Custom Options**: Browser-specific settings

### 4. Web Traveller

The **Web Traveller** is the execution engine that runs your journeys.

```java
WebTraveller traveller = new WebTraveller(options);
traveller.travelJourney(journey);
```

**Responsibilities:**
- Browser lifecycle management
- Error handling and reporting
- Logging and debugging
- Resource cleanup

### 5. Journey Context

The **Journey Context** provides access to the browser and other resources during journey execution.

```java
public class MyPageConsumer implements IPageConsumer<String> {
    @Override
    public String consumePage(IJourneyContext context) {
        // Access the browser
        IBrowser browser = context.getBrowser();
        
        // Find elements and extract data
        String title = browser.findElement("h1").getText();
        
        // Access journey breadcrumb for debugging
        IJourneyBreadcrumb breadcrumb = context.getJourneyBreadcrumb();
        
        return title;
    }
}
```

## Advanced Concepts

### 6. Page Consumers

**Page Consumers** extract data from web pages. They implement the `IPageConsumer<T>` interface.

```java
public class ProductPageConsumer implements IPageConsumer<Product> {
    @Override
    public Product consumePage(IJourneyContext context) {
        IBrowser browser = context.getBrowser();
        
        String name = browser.findElement(".product-name").getText();
        String price = browser.findElement(".price").getText();
        String description = browser.findElement(".description").getText();
        
        return new Product(name, price, description);
    }
}
```

**Best practices:**
- Keep consumers focused on a single page type
- Use strongly-typed return values
- Handle missing elements gracefully
- Extract all needed data in one pass

### 7. Conditional Actions

Execute actions based on page conditions:

```java
.conditionally()
    .when(context -> context.getBrowser().elementExists(".cookie-banner"))
    .then(context -> context.clickButton(".accept-cookies"))
```

**Use cases:**
- Handle optional elements (cookie banners, popups)
- Different paths based on page content
- Responsive design variations

### 8. Repeated Actions

Process multiple similar elements:

```java
.repeatFor(".product-item")
    .limited(10)  // Process max 10 items
    .action(context -> {
        WebElement item = context.getCurrentElement();
        String name = item.findElement(".name").getText();
        System.out.println("Product: " + name);
        return ActionResult.success();
    })
```

**Use cases:**
- Processing lists of items
- Pagination handling
- Bulk data extraction

### 9. Sub-Journeys

Break complex workflows into reusable pieces:

```java
IJourney loginJourney = JourneyBuilder.start()
    .navigateTo("/login")
    .completeForm("#loginForm", form -> form
        .setValue("username", username)
        .setValue("password", password))
    .clickButton("#loginBtn")
    .waitFor(".dashboard")
    .build();

IJourney mainJourney = JourneyBuilder.start()
    .executeSubJourney(loginJourney)
    .navigateTo("/products")
    .consumePage(ProductListConsumer.class)
    .build();
```

**Benefits:**
- Code reusability
- Easier testing
- Modular design
- Simplified maintenance

### 10. Error Handling

WebJourney provides comprehensive error handling:

```java
try {
    traveller.travelJourney(journey);
} catch (JourneyException e) {
    // Get detailed error information
    String errorMessage = e.getMessage();
    IJourneyBreadcrumb breadcrumb = e.getBreadcrumb();
    
    // Log or handle the error
    System.err.println("Journey failed at: " + breadcrumb);
}
```

**Error types:**
- **Element not found**: Required elements missing from page
- **Timeout errors**: Pages or elements taking too long to load
- **Browser errors**: Browser crashes or connectivity issues
- **Custom errors**: Your own validation failures

## Design Patterns

### Builder Pattern

WebJourney uses the builder pattern extensively for fluent APIs:

```java
// Journey building
IJourney journey = JourneyBuilder.start()
    .navigateTo(url)
    .clickButton(selector)
    .build();

// Option building
TravelOptions options = TravelOptions.builder()
    .withBrowser(BrowserType.CHROME)
    .withHeadless(true)
    .build();
```

### Page Object Pattern

Combine WebJourney with page objects for better organization:

```java
public class LoginPage {
    private static final String USERNAME_FIELD = "#username";
    private static final String PASSWORD_FIELD = "#password";
    private static final String LOGIN_BUTTON = "#loginBtn";
    
    public static IJourney login(String username, String password) {
        return JourneyBuilder.start()
            .completeForm("#loginForm", form -> form
                .setValue(USERNAME_FIELD, username)
                .setValue(PASSWORD_FIELD, password))
            .clickButton(LOGIN_BUTTON)
            .waitFor(".dashboard")
            .build();
    }
}
```

### Data Transfer Objects

Use DTOs for structured data extraction:

```java
public record UserProfile(
    String name,
    String email,
    String joinDate,
    List<String> interests
) {}

public class ProfilePageConsumer implements IPageConsumer<UserProfile> {
    @Override
    public UserProfile consumePage(IJourneyContext context) {
        IBrowser browser = context.getBrowser();
        
        String name = browser.findElement(".profile-name").getText();
        String email = browser.findElement(".profile-email").getText();
        String joinDate = browser.findElement(".join-date").getText();
        
        List<String> interests = browser.findElements(".interest-tag")
            .stream()
            .map(WebElement::getText)
            .collect(Collectors.toList());
        
        return new UserProfile(name, email, joinDate, interests);
    }
}
```

## Best Practices

### 1. Journey Design
- **Keep journeys focused**: One journey, one purpose
- **Use descriptive names**: Make journeys self-documenting
- **Plan for failure**: Include proper error handling
- **Make them reusable**: Parameterize common workflows

### 2. Element Selection
- **Prefer stable selectors**: Use IDs and data attributes over CSS classes
- **Use explicit waits**: Don't rely only on implicit waits
- **Handle dynamic content**: Wait for specific conditions

### 3. Data Extraction
- **Extract all needed data**: Minimize page visits
- **Use typed objects**: Strong typing prevents errors
- **Handle missing data**: Graceful degradation

### 4. Error Handling
- **Be specific**: Catch and handle specific error types
- **Provide context**: Include meaningful error messages
- **Log appropriately**: Help with debugging and monitoring

## Next Steps

Now that you understand WebJourney's core concepts:

1. **[Create Your First Journey](./first-journey.md)** - Put concepts into practice
2. **[Explore Examples](../../../examples/basic/)** - See real-world applications
3. **[Advanced Patterns](../advanced/journey-patterns.md)** - Learn sophisticated techniques

Understanding these concepts will help you build robust, maintainable web automation solutions with WebJourney! 
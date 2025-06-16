# WebJourney Basic Examples

This directory contains simple, practical examples to help you get started with WebJourney.

## Examples Overview

### 🚀 Quick Start Examples

- **[SimpleNavigation.java](./SimpleNavigation.java)** - Basic page navigation and data extraction
- **[FormSubmission.java](./FormSubmission.java)** - Fill out and submit web forms
- **[ElementInteraction.java](./ElementInteraction.java)** - Click buttons, links, and interact with elements
- **[WaitingForElements.java](./WaitingForElements.java)** - Handle dynamic content and loading states

### 📊 Data Extraction Examples

- **[PageDataExtraction.java](./PageDataExtraction.java)** - Extract text, attributes, and structured data
- **[ListProcessing.java](./ListProcessing.java)** - Process multiple similar elements
- **[TableDataExtraction.java](./TableDataExtraction.java)** - Extract data from HTML tables

### 🔄 Workflow Examples

- **[LoginWorkflow.java](./LoginWorkflow.java)** - Complete login flow with error handling
- **[SearchAndFilter.java](./SearchAndFilter.java)** - Search functionality and result processing
- **[MultiPageWorkflow.java](./MultiPageWorkflow.java)** - Navigate across multiple pages

## Running the Examples

### Prerequisites

1. **Java 21+** installed
2. **Maven 3.6+** installed
3. **Chrome browser** (examples use Chrome by default)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/jamoamo/webjourney.git
   cd webjourney/examples/basic
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

### Running Examples

Each example can be run as a standalone Java application:

```bash
# Run a specific example
mvn compile exec:java -Dexec.mainClass="SimpleNavigation"

# Or compile and run manually
mvn compile
java -cp target/classes SimpleNavigation
```

### Example Configuration

Most examples use these default settings:
- **Browser**: Chrome (headless mode)
- **Timeout**: 30 seconds
- **Wait Strategy**: Implicit wait of 10 seconds

You can modify these settings in each example's `TravelOptions` configuration.

## Example Code Structure

Each example follows this pattern:

```java
public class ExampleName {
    public static void main(String[] args) {
        // 1. Configure travel options
        TravelOptions options = TravelOptions.builder()
            .withBrowser(BrowserType.CHROME)
            .withHeadless(true)
            .build();
        
        // 2. Build the journey
        IJourney journey = JourneyBuilder.start()
            .navigateTo("https://example.com")
            // ... actions ...
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
```

## Key Concepts Demonstrated

### Navigation
```java
.navigateTo("https://example.com")     // Go to URL
.navigateBack()                        // Browser back
.refreshPage()                         // Refresh current page
```

### Element Interaction
```java
.clickButton("#submit-btn")            // Click by selector
.clickLink("a[href='/about']")        // Click specific link
```

### Form Handling
```java
.completeForm("#contact-form", form -> form
    .setValue("name", "John Doe")
    .setValue("email", "john@example.com")
    .setValue("message", "Hello!"))
```

### Data Extraction
```java
.consumePage(context -> {
    IBrowser browser = context.getBrowser();
    String title = browser.findElement("h1").getText();
    return title;
})
```

### Waiting for Elements
```java
.waitFor(".content")                   // Wait for element to appear
.waitFor(".loading", Duration.ofSeconds(10))  // Wait with timeout
```

## Common Patterns

### Error Handling
```java
try {
    traveller.travelJourney(journey);
} catch (JourneyException e) {
    System.err.println("Error: " + e.getMessage());
    // Handle specific error types
    if (e.getCause() instanceof TimeoutException) {
        System.err.println("Page took too long to load");
    }
}
```

### Conditional Logic
```java
.conditionally()
    .when(context -> context.getBrowser().elementExists(".cookie-banner"))
    .then(context -> context.clickButton(".accept-cookies"))
```

### Page Object Pattern
```java
public class LoginPage {
    public static IJourney login(String username, String password) {
        return JourneyBuilder.start()
            .navigateTo("/login")
            .completeForm("#login-form", form -> form
                .setValue("username", username)
                .setValue("password", password))
            .clickButton("#login-btn")
            .waitFor(".dashboard")
            .build();
    }
}
```

## Troubleshooting

### Common Issues

#### "WebDriver not found"
- Ensure Chrome is installed
- WebJourney will automatically download ChromeDriver

#### "Element not found"
- Check CSS selectors are correct
- Add explicit waits for dynamic content
- Verify the element exists on the page

#### "Timeout errors"
- Increase timeout values in TravelOptions
- Use more specific wait conditions
- Check network connectivity

### Debug Mode

Run examples with debug logging:
```bash
java -Dlog4j.level=DEBUG -cp target/classes SimpleNavigation
```

## Next Steps

After running these basic examples:

1. **[Advanced Examples](../advanced/)** - More complex scenarios
2. **[Spring Boot Examples](../spring-boot/)** - Integration with Spring Boot
3. **[User Guide](../../docs/user-guide/)** - Comprehensive documentation
4. **[API Reference](../../docs/api/)** - Complete API documentation

## Contributing

Found an issue or have a suggestion for improving these examples?

- Report issues on [GitHub Issues](https://github.com/jamoamo/webjourney/issues)
- Contribute new examples via [Pull Requests](https://github.com/jamoamo/webjourney/pulls)
- Follow our [Contributing Guide](../../docs/contributing/)

Happy automating! 🚀 
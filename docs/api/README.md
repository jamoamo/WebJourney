# WebJourney API Reference

Complete reference documentation for the WebJourney API.

## Core API

### Main Classes

#### [WebTraveller](./core/web-traveller.md)
The main execution engine for running web journeys.

```java
WebTraveller traveller = new WebTraveller(options);
traveller.travelJourney(journey);
```

#### [JourneyBuilder](./core/journey-builder.md)
Fluent API for creating web journeys.

```java
IJourney journey = JourneyBuilder.start()
    .navigateTo("https://example.com")
    .clickButton("#submit")
    .build();
```

#### [TravelOptions](./core/travel-options.md)
Configuration for journey execution.

```java
TravelOptions options = TravelOptions.builder()
    .withBrowser(BrowserType.CHROME)
    .withHeadless(true)
    .build();
```

### Core Interfaces

- **[IJourney](./interfaces/ijourney.md)** - Represents a complete web automation workflow
- **[IJourneyContext](./interfaces/ijourney-context.md)** - Provides access to browser and execution context
- **[IPageConsumer](./interfaces/ipage-consumer.md)** - Interface for extracting data from web pages
- **[IBrowser](./interfaces/ibrowser.md)** - Abstraction over browser interactions

## Actions Reference

WebJourney provides comprehensive action types for web automation:

### Navigation Actions
- **[NavigateAction](./actions/navigate-action.md)** - Navigate to URLs
- **[BackNavigationTarget](./actions/back-navigation.md)** - Browser back navigation
- **[ForwardNavigationTarget](./actions/forward-navigation.md)** - Browser forward navigation
- **[RefreshNavigationTarget](./actions/refresh-navigation.md)** - Page refresh

### Interaction Actions
- **[ClickButtonAction](./actions/click-button-action.md)** - Click elements
- **[CompleteFormAction](./actions/complete-form-action.md)** - Fill out forms
- **[ConsumePageAction](./actions/consume-page-action.md)** - Extract data from pages

### Control Flow Actions
- **[ConditionalAction](./actions/conditional-action.md)** - Conditional execution
- **[RepeatedAction](./actions/repeated-action.md)** - Process multiple elements
- **[SubJourney](./actions/sub-journey.md)** - Execute nested journeys

## Configuration

### Browser Configuration
- **[BrowserType](./config/browser-type.md)** - Supported browser types
- **[BrowserOptions](./config/browser-options.md)** - Browser-specific settings
- **[DefaultBrowserOptions](./config/default-browser-options.md)** - Default configurations

### Timeouts and Waits
- **[Timeout Configuration](./config/timeouts.md)** - Page load and element timeouts
- **[Wait Strategies](./config/wait-strategies.md)** - Different waiting approaches

## Error Handling

### Exception Types
- **[JourneyException](./exceptions/journey-exception.md)** - Base journey exception
- **[BaseJourneyActionException](./exceptions/action-exception.md)** - Action-specific errors
- **[JourneyBuilderException](./exceptions/builder-exception.md)** - Journey building errors

### Error Recovery
- **[IUnavailableTargetStrategy](./error-handling/unavailable-target-strategy.md)** - Handle missing elements
- **[ErrorUnavailableTargetStrategy](./error-handling/error-strategy.md)** - Fail on missing elements
- **[IgnoreUnavailableTargetStrategy](./error-handling/ignore-strategy.md)** - Continue on missing elements

## Observers and Extensibility

### Journey Observers
- **[IJourneyObserver](./observers/ijourney-observer.md)** - Monitor journey execution
- **[Journey Events](./observers/journey-events.md)** - Available observation points

### Extension Points
- **[Custom Actions](./extensions/custom-actions.md)** - Creating custom action types
- **[Custom Browsers](./extensions/custom-browsers.md)** - Adding browser support
- **[Custom Strategies](./extensions/custom-strategies.md)** - Custom error handling

## Testing Support

### WebJourney Test Module
- **[TestCreator](./testing/test-creator.md)** - Create test instances
- **[CreationResult](./testing/creation-result.md)** - Test creation results
- **[TestException](./testing/test-exception.md)** - Testing errors

## Annotations

### Entity Mapping
- **[@Entity](./annotations/entity.md)** - Mark classes for data extraction
- **[@Field](./annotations/field.md)** - Map form fields
- **[@Selector](./annotations/selector.md)** - Define element selectors

## Quick Reference

### Common Patterns

#### Basic Journey
```java
IJourney journey = JourneyBuilder.start()
    .navigateTo("https://example.com")
    .waitFor(".content")
    .consumePage(PageConsumer.class)
    .build();
```

#### Form Submission
```java
.completeForm("#myForm", form -> form
    .setValue("username", "testuser")
    .setValue("password", "testpass")
    .setValue("email", "test@example.com"))
.clickButton("#submitBtn")
```

#### Conditional Logic
```java
.conditionally()
    .when(context -> context.getBrowser().elementExists(".modal"))
    .then(context -> context.clickButton(".modal .close"))
```

#### Data Extraction
```java
.consumePage(context -> {
    IBrowser browser = context.getBrowser();
    return new ProductData(
        browser.findElement(".title").getText(),
        browser.findElement(".price").getText()
    );
})
```

## Version Information

- **Current Version:** 3.3.1-SNAPSHOT
- **Java Version:** 21+
- **Selenium Version:** 4.33.0

## See Also

- **[User Guide](../user-guide/)** - Learn how to use WebJourney
- **[Examples](../../examples/)** - Practical code examples
- **[Contributing](../contributing/)** - How to contribute to WebJourney 
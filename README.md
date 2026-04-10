![webjourney maven](https://img.shields.io/maven-central/v/io.github.jamoamo/webjourney)Includes a test utility (WIP) to test web interactions without a real browser/web page.![webjourney-test maven](https://img.shields.io/maven-central/v/io.github.jamoamo/webjourney-test)
# Web Journey

## Overview
Java library for automating web interactions via the concept of a sequence of actions on the web, defined as a web journey. Define the Path and interactions and let it run.

Built on top of Selenium for interacting with web pages.
## [WIP] Test Utility

Inlcudes a test utility (WIP) to test web interactions without a real browser\web page.

## Retries

WebJourney includes robust, customizable retry capabilities via `failsafe` to handle transient network errors during navigation or action execution.

**Global Retry Policy via Builder**
```java
IJourney journey = JourneyBuilder.builder()
    .options()
        .retryPolicy()
            .maxRetries(3)
            .delay(Duration.ofSeconds(1)) // Failsafe retry delay
            .build()
        .apply()
    .build();
```

**Operation-Specific Retry Annotations**
For finer control when extracting nested entities, apply the `@Retry` annotation to `@ExtractFromUrl` fields. This overrides the default policy just for that navigation action:
```java
public class MyEntity {
    @ExtractFromUrl
    @Retry(maxRetries = 5, delayMs = 2000)
    private NestedEntity details;
}
```

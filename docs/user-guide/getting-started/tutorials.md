# WebJourney Tutorials

This section provides step-by-step tutorials to guide you through using WebJourney.

## 1. Getting Started: Your First Web Journey

This tutorial will walk you through creating a simple WebJourney that navigates to a website and extracts some basic information.

### Prerequisites
*   Java Development Kit (JDK) 21 or later installed.
*   Maven installed.
*   A basic understanding of Java.

### Step 1: Create a New Maven Project

First, let's create a new Maven project. Open your terminal or command prompt and run the following command:

```bash
mvn archetype:generate -DgroupId=com.example.webjourney -DartifactId=my-first-journey -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
```

This command creates a new project directory named `my-first-journey`.

### Step 2: Add WebJourney Dependency

Navigate into your new project directory:

```bash
cd my-first-journey
```

Now, open the `pom.xml` file and add the WebJourney dependency. Make sure to use the latest version available (check Maven Central for the most recent version).

```xml
<dependencies>
    <!-- WebJourney Core Dependency -->
    <dependency>
        <groupId>io.github.jamoamo</groupId>
        <artifactId>webjourney</artifactId>
        <version>3.3.1-SNAPSHOT</version> <!-- Use the latest version -->
    </dependency>

    <!-- Selenium WebDriver (Chrome) -->
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
        <version>4.33.0</version> <!-- Use the Selenium version compatible with WebJourney -->
    </dependency>

    <!-- SLF4J for logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.13</version> <!-- Or any other SLF4J binding -->
    </dependency>

    <!-- Test containers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.8</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.8</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>nginx</artifactId>
        <version>1.19.8</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>selenium</artifactId>
        <version>1.19.8</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Step 3: Create Your First Journey

Open the `src/main/java/com/example/webjourney/App.java` file and replace its content with the following code. This code will define a simple journey to navigate to `example.com` and print its title.

```java
package com.example.webjourney;

import io.github.jamoamo.webjourney.JourneyBuilder;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle;
import io.github.jamoamo.webjourney.annotation.ExtractValue;
import java.util.concurrent.atomic.AtomicReference;

public class App {

    public static class ExamplePageEntity {
        @ExtractFromWindowTitle
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting WebJourney example...");

        AtomicReference<ExamplePageEntity> extractedData = new AtomicReference<>();

        IJourney journey = JourneyBuilder.path()
                .navigateTo("https://example.com")
                .consumePage(ExamplePageEntity.class, extractedData::set)
                .build();

        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);

        ExamplePageEntity entity = extractedData.get();
        if (entity != null) {
            System.out.println("Page Title: " + entity.getTitle());
        } else {
            System.out.println("Failed to extract page data.");
        }

        System.out.println("WebJourney example finished.");
    }
}
```

### Step 4: Run the Journey

Compile and run your project using Maven:

```bash
mvn clean install exec:java -Dexec.mainClass="com.example.webjourney.App"
```

You should see a Chrome browser window open, navigate to `example.com`, and then close. The console output will show the extracted page title.

```
Starting WebJourney example...
... (Selenium and WebJourney logs) ...
Page Title: Example Domain
WebJourney example finished.
```

This completes your first step-by-step WebJourney tutorial! You have successfully navigated to a website and extracted its title. 
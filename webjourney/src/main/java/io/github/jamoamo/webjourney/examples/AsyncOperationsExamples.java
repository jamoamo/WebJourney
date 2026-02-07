/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.examples;

import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.*;
import io.github.jamoamo.webjourney.reserved.selenium.ChromeBrowserFactory;
import io.github.jamoamo.webjourney.ParallelJourneyBuilder;
import io.github.jamoamo.webjourney.ParallelConsumePageAction;
import io.github.jamoamo.webjourney.ParallelNavigateAndConsumeAction;
import io.github.jamoamo.webjourney.api.PageConsumerException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Examples demonstrating the new asynchronous operations in WebJourney.
 * These examples show how to:
 * 1. Fetch different values from a page in parallel
 * 2. Fetch values from linked pages in parallel
 * 
 * @author James Amoore
 */
public class AsyncOperationsExamples
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncOperationsExamples.class);
    
    /**
     * Example 1: Parallel Page Consumption
     * Demonstrates how to extract multiple field values from a single page concurrently.
     * This is useful for pages with many independent fields that can be extracted simultaneously.
     */
    public static void parallelPageConsumptionExample()
    {
        LOGGER.info("=== Parallel Page Consumption Example ===");
        
        // Create travel options with Chrome browser
        TravelOptions travelOptions = new TravelOptions();
        travelOptions.setPreferredBrowserStrategy(
            new PreferredBrowserStrategy(new ChromeBrowserFactory())
        );
        
        // Create WebTraveller
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        // Create a journey that demonstrates parallel page consumption
        IJourney journey = context -> {
            LOGGER.info("Starting parallel page consumption journey...");
            
            // Example: Navigate to a page with many fields
            // In a real scenario, you'd navigate to an actual page
            LOGGER.info("Navigated to example page with multiple fields");
            
            // Create parallel consume page action
            // This will extract field values concurrently instead of sequentially
            ParallelConsumePageAction<ExamplePageEntity> parallelAction = 
                new ParallelConsumePageAction<>(ExamplePageEntity.class, page -> {
                    LOGGER.info("Processing page entity with parallel extraction...");
                    
                    // The entity extraction will happen in parallel for independent fields
                    // This is handled internally by the ParallelConsumePageAction
                    
                    LOGGER.info("Page processing completed");
                }, 4); // Use 4 concurrent threads for field extraction
            
            // Execute the parallel action
            try
            {
                parallelAction.executeAction(context);
                LOGGER.info("Parallel page consumption completed successfully");
            }
            catch(Exception ex)
            {
                LOGGER.error("Parallel page consumption failed", ex);
                throw new RuntimeException(ex);
            }
            finally
            {
                parallelAction.shutdown();
            }
        };
        
        // Execute the journey
        try
        {
            traveller.travelJourney(journey);
            LOGGER.info("Journey completed successfully");
        }
        catch(Exception ex)
        {
            LOGGER.error("Journey failed", ex);
        }
    }
    
    /**
     * Example 2: Parallel Navigation and Page Consumption
     * Demonstrates how to navigate to multiple linked pages concurrently and extract data in parallel.
     * This is useful for scenarios where you need to fetch data from multiple related pages simultaneously.
     */
    public static void parallelNavigationExample()
    {
        LOGGER.info("=== Parallel Navigation Example ===");
        
        // Create travel options with Chrome browser
        TravelOptions travelOptions = new TravelOptions();
        travelOptions.setPreferredBrowserStrategy(
            new PreferredBrowserStrategy(new ChromeBrowserFactory())
        );
        
        // Create WebTraveller
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        // Create a journey that demonstrates parallel navigation
        IJourney journey = context -> {
            LOGGER.info("Starting parallel navigation journey...");
            
            try
            {
                // Example: List of URLs to navigate to in parallel
                List<URL> urls = new ArrayList<>();
                urls.add(new URL("https://example.com/page1"));
                urls.add(new URL("https://example.com/page2"));
                urls.add(new URL("https://example.com/page3"));
                urls.add(new URL("https://example.com/page4"));
                
                LOGGER.info("Preparing to navigate to {} pages in parallel", urls.size());
                
                // Create parallel navigate and consume action
                ParallelNavigateAndConsumeAction<ExamplePageEntity> parallelAction = 
                    new ParallelNavigateAndConsumeAction<>(urls, ExamplePageEntity.class, page -> {
                        LOGGER.info("Processing page from parallel navigation...");
                        
                        // Process the page entity
                        // This happens concurrently across multiple browser instances
                        
                        LOGGER.info("Page processing completed");
                    }, new ChromeBrowserFactory(), 4); // Use 4 concurrent browser instances
                
                // Execute the parallel action
                parallelAction.executeAction(context);
                LOGGER.info("Parallel navigation and consumption completed successfully");
                
                // Clean up
                parallelAction.shutdown();
            }
            catch(Exception ex)
            {
                LOGGER.error("Parallel navigation failed", ex);
                throw new RuntimeException(ex);
            }
        };
        
        // Execute the journey
        try
        {
            traveller.travelJourney(journey);
            LOGGER.info("Journey completed successfully");
        }
        catch(Exception ex)
        {
            LOGGER.error("Journey failed", ex);
        }
    }
    
    /**
     * Example 3: Advanced Parallel Journey with Dependencies
     * Demonstrates how to use the ParallelJourneyBuilder to create complex journeys
     * with actions that can run in parallel while respecting dependencies.
     */
    public static void advancedParallelJourneyExample()
    {
        LOGGER.info("=== Advanced Parallel Journey Example ===");
        
        // Create travel options with Chrome browser
        TravelOptions travelOptions = new TravelOptions();
        travelOptions.setPreferredBrowserStrategy(
            new PreferredBrowserStrategy(new ChromeBrowserFactory())
        );
        
        // Create WebTraveller
        WebTraveller traveller = new WebTraveller(travelOptions);
        
        // Create a journey using ParallelJourneyBuilder
        IJourney journey = context -> {
            LOGGER.info("Starting advanced parallel journey...");
            
            try
            {
                // Create parallel journey builder
                ParallelJourneyBuilder builder = new ParallelJourneyBuilder(4); // 4 concurrent actions
                
                // Add actions that can run in parallel
                // These actions don't depend on each other and can execute concurrently
                builder.addAction(new ExampleParallelAction("Action1", 1000))
                       .addAction(new ExampleParallelAction("Action2", 1500))
                       .addAction(new ExampleParallelAction("Action3", 800))
                       .addAction(new ExampleParallelAction("Action4", 1200));
                
                // Add dependency: Action5 depends on Action1 and Action2
                builder.addAction(new ExampleParallelAction("Action5", 500))
                       .addDependency("Action5", "Action1")
                       .addDependency("Action5", "Action2");
                
                // Add more actions that can run in parallel
                builder.addAction(new ExampleParallelAction("Action6", 900))
                       .addAction(new ExampleParallelAction("Action7", 1100));
                
                // Build and execute the parallel journey
                IJourney parallelJourney = builder.build();
                parallelJourney.doJourney(context);
                
                LOGGER.info("Advanced parallel journey completed successfully");
                
                // Clean up
                builder.shutdown();
            }
            catch(Exception ex)
            {
                LOGGER.error("Advanced parallel journey failed", ex);
                throw new RuntimeException(ex);
            }
        };
        
        // Execute the journey
        try
        {
            traveller.travelJourney(journey);
            LOGGER.info("Journey completed successfully");
        }
        catch(Exception ex)
        {
            LOGGER.error("Journey failed", ex);
        }
    }
    
    /**
     * Example 4: Performance Comparison
     * Demonstrates the performance benefits of parallel execution vs sequential execution.
     */
    public static void performanceComparisonExample()
    {
        LOGGER.info("=== Performance Comparison Example ===");
        
        // Test sequential execution
        long startTime = System.currentTimeMillis();
        sequentialExecution();
        long sequentialTime = System.currentTimeMillis() - startTime;
        
        // Test parallel execution
        startTime = System.currentTimeMillis();
        parallelExecution();
        long parallelTime = System.currentTimeMillis() - startTime;
        
        LOGGER.info("Performance Results:");
        LOGGER.info("Sequential execution time: {} ms", sequentialTime);
        LOGGER.info("Parallel execution time: {} ms", parallelTime);
        LOGGER.info("Speedup: {:.2f}x", (double) sequentialTime / parallelTime);
    }
    
    private static void sequentialExecution()
    {
        LOGGER.info("Executing actions sequentially...");
        
        // Simulate sequential execution of 4 actions, each taking 1 second
        for(int i = 1; i <= 4; i++)
        {
            try
            {
                LOGGER.info("Executing action {} sequentially", i);
                Thread.sleep(1000); // Simulate 1 second work
                LOGGER.info("Action {} completed", i);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private static void parallelExecution()
    {
        LOGGER.info("Executing actions in parallel...");
        
        // Simulate parallel execution of 4 actions, each taking 1 second
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try
        {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for(int i = 1; i <= 4; i++)
            {
                final int actionNumber = i;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try
                    {
                        LOGGER.info("Executing action {} in parallel", actionNumber);
                        Thread.sleep(1000); // Simulate 1 second work
                        LOGGER.info("Action {} completed", actionNumber);
                    }
                    catch(InterruptedException ex)
                    {
                        Thread.currentThread().interrupt();
                    }
                }, executor);
                
                futures.add(future);
            }
            
            // Wait for all actions to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        finally
        {
            executor.shutdown();
        }
    }
    
    /**
     * Main method to run all examples.
     */
    public static void main(String[] args)
    {
        LOGGER.info("Starting WebJourney Async Operations Examples");
        
        try
        {
            // Run examples
            parallelPageConsumptionExample();
            Thread.sleep(2000); // Brief pause between examples
            
            parallelNavigationExample();
            Thread.sleep(2000); // Brief pause between examples
            
            advancedParallelJourneyExample();
            Thread.sleep(2000); // Brief pause between examples
            
            performanceComparisonExample();
            
            LOGGER.info("All examples completed successfully");
        }
        catch(Exception ex)
        {
            LOGGER.error("Error running examples", ex);
        }
    }
    
    /**
     * Example page entity class for demonstration purposes.
     * In a real scenario, this would be annotated with field extraction annotations.
     */
    public static class ExamplePageEntity
    {
        private String title;
        private String content;
        private String author;
        private String date;
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
    
    /**
     * Example parallel action for demonstration purposes.
     */
    private static class ExampleParallelAction extends AWebAction
    {
        private final String actionName;
        private final long executionTime;
        
        public ExampleParallelAction(String actionName, long executionTime)
        {
            this.actionName = actionName;
            this.executionTime = executionTime;
        }
        
        @Override
        protected ActionResult executeActionImpl(IJourneyContext context)
        {
            try
            {
                LOGGER.info("Executing {} (simulated work: {} ms)", actionName, executionTime);
                Thread.sleep(executionTime);
                LOGGER.info("Completed {}", actionName);
                return ActionResult.SUCCESS;
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
                return ActionResult.FAILURE;
            }
        }
        
        @Override
        protected String getActionName()
        {
            return actionName;
        }
    }
}
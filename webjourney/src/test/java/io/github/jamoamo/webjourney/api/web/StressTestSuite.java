/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.api.web;

import io.github.jamoamo.webjourney.JourneyContext;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-load stress tests for memory safety and extended concurrent execution.
 */
@Tag("stress")
public class StressTestSuite
{
    @Test
    public void memoryLeakTest_extendedExecution() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Baseline memory usage
        System.gc();
        Thread.sleep(100);
        MemoryUsage baselineHeap = memoryBean.getHeapMemoryUsage();
        long baselineUsed = baselineHeap.getUsed();
        
        int iterations = 10000;
        int batchSize = 100;
        
        for (int batch = 0; batch < iterations / batchSize; batch++)
        {
            ExecutorService executor = Executors.newFixedThreadPool(batchSize);
            CountDownLatch latch = new CountDownLatch(batchSize);
            
            for (int i = 0; i < batchSize; i++)
            {
                final int operationId = batch * batchSize + i;
                executor.submit(() -> {
                    try
                    {
                        JourneyContext context = new JourneyContext();
                        context.getBrowserArguments().addGlobal(List.of("--operation-" + operationId));
                        context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, 
                            List.of("--chrome-op-" + operationId));
                        
                        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                        
                        // Use the result to prevent optimization
                        assertNotNull(resolved);
                        assertTrue(resolved.getArguments().size() > 0);
                    }
                    finally
                    {
                        latch.countDown();
                    }
                });
            }
            
            assertTrue(latch.await(30, TimeUnit.SECONDS), 
                "Batch " + batch + " should complete within 30 seconds");
            executor.shutdown();
            
            // Periodic GC and memory check
            if (batch % 10 == 0)
            {
                System.gc();
                Thread.sleep(50);
                MemoryUsage currentHeap = memoryBean.getHeapMemoryUsage();
                long currentUsed = currentHeap.getUsed();
                long memoryIncrease = currentUsed - baselineUsed;
                
                // Allow for some memory growth, but not excessive
                long maxAllowedIncrease = 50 * 1024 * 1024; // 50MB
                assertTrue(memoryIncrease < maxAllowedIncrease,
                    "Memory usage should not increase excessively. Baseline: " + baselineUsed + 
                    ", Current: " + currentUsed + ", Increase: " + memoryIncrease + " bytes");
            }
        }
        
        // Final memory check
        System.gc();
        Thread.sleep(100);
        MemoryUsage finalHeap = memoryBean.getHeapMemoryUsage();
        long finalUsed = finalHeap.getUsed();
        long totalIncrease = finalUsed - baselineUsed;
        
        // Memory should not have grown significantly
        long maxTotalIncrease = 100 * 1024 * 1024; // 100MB
        assertTrue(totalIncrease < maxTotalIncrease,
            "Total memory increase should be reasonable. Baseline: " + baselineUsed + 
            ", Final: " + finalUsed + ", Increase: " + totalIncrease + " bytes");
    }

    @Test
    public void highConcurrencyBurst() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider();
        
        int burstThreads = 500;
        ExecutorService executor = Executors.newFixedThreadPool(burstThreads);
        CountDownLatch latch = new CountDownLatch(burstThreads);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        AtomicLong totalOperations = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int t = 0; t < burstThreads; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    JourneyContext context = new JourneyContext();
                    context.getBrowserArguments().addGlobal(List.of("--burst-" + threadId));
                    
                    ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                    
                    assertNotNull(resolved);
                    assertTrue(resolved.getArguments().contains("--burst-" + threadId));
                    totalOperations.incrementAndGet();
                }
                catch (Exception e)
                {
                    exceptions.add(e);
                }
                finally
                {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), 
            "High concurrency burst should complete within 60 seconds");
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        if (!exceptions.isEmpty())
        {
            fail("High concurrency burst failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        assertEquals(burstThreads, totalOperations.get(), "All operations should complete successfully");
        
        // Performance assertion - should handle 500 concurrent operations reasonably quickly
        assertTrue(duration < 30000, "Should complete 500 concurrent operations in under 30 seconds, took: " + duration + "ms");
    }

    @Test
    public void sustainedLoad_longRunning() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider();
        
        int duration = 30; // seconds
        int threadCount = 50;
        AtomicLong operationsCompleted = new AtomicLong(0);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (duration * 1000);
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    int operationCount = 0;
                    while (System.currentTimeMillis() < endTime && !Thread.currentThread().isInterrupted())
                    {
                        JourneyContext context = new JourneyContext();
                        context.getBrowserArguments().addGlobal(List.of("--sustained-" + threadId + "-" + operationCount));
                        
                        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                        
                        assertNotNull(resolved);
                        operationsCompleted.incrementAndGet();
                        operationCount++;
                        
                        // Small delay to simulate realistic usage
                        Thread.sleep(10);
                    }
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e)
                {
                    exceptions.add(e);
                }
            });
        }
        
        // Wait for test duration plus grace period
        Thread.sleep((duration + 5) * 1000);
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        
        if (!exceptions.isEmpty())
        {
            fail("Sustained load test failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        long totalOps = operationsCompleted.get();
        assertTrue(totalOps > 0, "Should complete some operations");
        
        // Should achieve reasonable throughput
        double opsPerSecond = (double) totalOps / duration;
        assertTrue(opsPerSecond > 10, "Should achieve at least 10 operations per second, achieved: " + opsPerSecond);
        
        System.out.println("Sustained load test completed " + totalOps + " operations in " + duration + 
                          " seconds (" + String.format("%.2f", opsPerSecond) + " ops/sec)");
    }

    @Test
    public void resourceExhaustion_recovery() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider();
        
        // Create many contexts with large argument lists to stress memory
        int contextCount = 1000;
        int argsPerContext = 100;
        
        List<JourneyContext> contexts = Collections.synchronizedList(new java.util.ArrayList<>());
        
        // Phase 1: Create many contexts with large argument sets
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch creationLatch = new CountDownLatch(contextCount);
        
        for (int i = 0; i < contextCount; i++)
        {
            final int contextId = i;
            executor.submit(() -> {
                try
                {
                    JourneyContext context = new JourneyContext();
                    for (int j = 0; j < argsPerContext; j++)
                    {
                        context.getBrowserArguments().addGlobal(List.of("--large-" + contextId + "-" + j));
                    }
                    contexts.add(context);
                }
                finally
                {
                    creationLatch.countDown();
                }
            });
        }
        
        assertTrue(creationLatch.await(60, TimeUnit.SECONDS), "Context creation should complete");
        
                        // Phase 2: Resolve arguments for all contexts concurrently
        CountDownLatch resolutionLatch = new CountDownLatch(contextCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (JourneyContext context : contexts)
        {
            executor.submit(() -> {
                try
                {
                    ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                    assertNotNull(resolved);
                    assertEquals(argsPerContext, resolved.getArguments().size());
                }
                catch (Exception e)
                {
                    exceptions.add(e);
                }
                finally
                {
                    resolutionLatch.countDown();
                }
            });
        }
        
        assertTrue(resolutionLatch.await(120, TimeUnit.SECONDS), "Resolution should complete");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Resource exhaustion test failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        // Phase 3: Verify system is still responsive after stress
        JourneyContext testContext = new JourneyContext();
        testContext.getBrowserArguments().addGlobal(List.of("--post-stress-test"));
        
        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, testContext);
        assertNotNull(resolved);
        assertTrue(resolved.getArguments().contains("--post-stress-test"));
    }
}

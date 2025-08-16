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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests for journey context isolation and thread-safety.
 */
public class ConcurrentJourneyTest
{
    @Test
    public void parallelJourneys_isolatedBrowserArguments() throws InterruptedException
    {
        int threadCount = 10;
        int journeysPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    for (int j = 0; j < journeysPerThread; j++)
                    {
                        JourneyContext context = new JourneyContext();
                        String uniqueArg = "--thread-" + threadId + "-journey-" + j;
                        context.getBrowserArguments().addGlobal(List.of(uniqueArg));
                        
                        // Verify isolation - each context should only see its own argument
                        List<String> snapshot = context.getBrowserArguments().snapshotGlobal();
                        assertTrue(snapshot.contains(uniqueArg), 
                            "Thread " + threadId + " journey " + j + " should contain its unique arg");
                        assertEquals(1, snapshot.size(), 
                            "Thread " + threadId + " journey " + j + " should only have 1 arg, but had: " + snapshot);
                    }
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
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Concurrent execution failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }

    @Test
    public void concurrentModificationsToSameJourneyContext() throws InterruptedException
    {
        JourneyContext context = new JourneyContext();
        int threadCount = 50;
        int argsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalAdded = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    for (int i = 0; i < argsPerThread; i++)
                    {
                        String arg = "--thread-" + threadId + "-arg-" + i;
                        context.getBrowserArguments().addGlobal(List.of(arg));
                        totalAdded.incrementAndGet();
                        
                        // Also test browser-specific args
                        context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, List.of(arg + "-chrome"));
                        totalAdded.incrementAndGet();
                    }
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
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Concurrent execution failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        // Verify all arguments were added
        List<String> globalArgs = context.getBrowserArguments().snapshotGlobal();
        List<String> chromeArgs = context.getBrowserArguments().snapshotForBrowser(StandardBrowser.CHROME);
        
        int expectedTotalGlobal = threadCount * argsPerThread;
        int expectedTotalChrome = threadCount * argsPerThread;
        
        assertEquals(expectedTotalGlobal, globalArgs.size(), 
            "Should have all global args added concurrently");
        assertEquals(expectedTotalChrome, chromeArgs.size(), 
            "Should have all Chrome args added concurrently");
        
        // Verify uniqueness (no duplicates lost due to race conditions)
        assertEquals(globalArgs.size(), globalArgs.stream().distinct().count(),
            "All global args should be unique");
        assertEquals(chromeArgs.size(), chromeArgs.stream().distinct().count(),
            "All Chrome args should be unique");
    }

    @Test
    public void journeyInputsConcurrentAccess() throws InterruptedException
    {
        JourneyContext context = new JourneyContext();
        int threadCount = 20;
        int inputsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    for (int i = 0; i < inputsPerThread; i++)
                    {
                        String key = "thread-" + threadId + "-input-" + i;
                        String value = "value-" + threadId + "-" + i;
                        
                        // Set input
                        context.setJourneyInput(key, value);
                        
                        // Immediately read it back
                        Object retrieved = context.getJourneyInput(key);
                        assertEquals(value, retrieved, 
                            "Should retrieve the same value that was just set");
                    }
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
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Concurrent execution failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        // Verify all inputs are present
        for (int t = 0; t < threadCount; t++)
        {
            for (int i = 0; i < inputsPerThread; i++)
            {
                String key = "thread-" + t + "-input-" + i;
                String expectedValue = "value-" + t + "-" + i;
                assertEquals(expectedValue, context.getJourneyInput(key),
                    "Input " + key + " should have correct value");
            }
        }
    }

    @RepeatedTest(5)
    public void stressTest_hundredConcurrentJourneys() throws InterruptedException
    {
        int journeyCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(journeyCount);
        CountDownLatch latch = new CountDownLatch(journeyCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int j = 0; j < journeyCount; j++)
        {
            final int journeyId = j;
            executor.submit(() -> {
                try
                {
                    JourneyContext context = new JourneyContext();
                    
                    // Simulate typical journey usage
                    context.getBrowserArguments().addGlobal(List.of("--journey-" + journeyId));
                    context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, 
                        List.of("--chrome-journey-" + journeyId));
                    context.setJourneyInput("journeyId", journeyId);
                    
                    // Verify state
                    List<String> global = context.getBrowserArguments().snapshotGlobal();
                    List<String> chrome = context.getBrowserArguments().snapshotForBrowser(StandardBrowser.CHROME);
                    Object inputValue = context.getJourneyInput("journeyId");
                    
                    assertEquals(1, global.size());
                    assertEquals(1, chrome.size());
                    assertEquals(journeyId, inputValue);
                    assertTrue(global.get(0).contains(String.valueOf(journeyId)));
                    assertTrue(chrome.get(0).contains(String.valueOf(journeyId)));
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
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All journeys should complete within 60 seconds");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Stress test failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }
}

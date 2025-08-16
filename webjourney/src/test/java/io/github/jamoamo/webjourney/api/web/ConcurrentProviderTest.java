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
import org.junit.jupiter.api.RepeatedTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests for browser arguments provider thread-safety.
 */
public class ConcurrentProviderTest
{
    @Test
    public void providerResolve_concurrentAccess() throws InterruptedException
    {
        // Setup provider with environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WEBJOURNEY_BROWSER_ARGS", "--global-env");
        envVars.put("WEBJOURNEY_CHROME_ARGS", "--chrome-env");
        Function<String, String> envFunction = envVars::get;
        
        AsyncConfiguration config = new AsyncConfiguration(
            List.of("--global-config"), 
            List.of("--chrome-config")
        );
        
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(envFunction, config);
        
        int threadCount = 20;
        int resolutionsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        List<ResolvedBrowserArguments> results = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    for (int i = 0; i < resolutionsPerThread; i++)
                    {
                        JourneyContext context = new JourneyContext();
                        context.getBrowserArguments().addGlobal(List.of("--thread-" + threadId + "-global"));
                        context.getBrowserArguments().addForBrowser(StandardBrowser.CHROME, 
                            List.of("--thread-" + threadId + "-chrome"));
                        
                        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                        results.add(resolved);
                        
                        // Verify expected structure
                        assertNotNull(resolved);
                        assertNotNull(resolved.getArguments());
                        assertNotNull(resolved.getProvenance());
                        assertTrue(resolved.getArguments().size() > 0);
                        assertEquals(resolved.getArguments().size(), resolved.getProvenance().size());
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
            fail("Concurrent provider access failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
        
        // Verify all results are present and consistent
        assertEquals(threadCount * resolutionsPerThread, results.size());
        
        // Each result should contain the expected base arguments plus thread-specific ones
        for (ResolvedBrowserArguments result : results)
        {
            List<String> args = result.getArguments();
            // Should contain global config, chrome config, env vars, and thread-specific args
            assertTrue(args.contains("--global-config"), "Should contain global config");
            assertTrue(args.contains("--chrome-config"), "Should contain chrome config");
            assertTrue(args.contains("--global-env"), "Should contain global env");
            assertTrue(args.contains("--chrome-env"), "Should contain chrome env");
            // Should contain at least one thread-specific arg
            assertTrue(args.stream().anyMatch(arg -> arg.contains("--thread-")), 
                "Should contain thread-specific args");
        }
    }

    @Test
    public void environmentVariableRaceCondition() throws InterruptedException
    {
        // Simulate changing environment variables during execution
        Map<String, String> envVars = Collections.synchronizedMap(new HashMap<>());
        envVars.put("WEBJOURNEY_BROWSER_ARGS", "--initial");
        Function<String, String> envFunction = envVars::get;
        
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider(envFunction);
        
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount + 1);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        // Thread that modifies environment during execution
        executor.submit(() -> {
            try
            {
                for (int i = 0; i < 50; i++)
                {
                    envVars.put("WEBJOURNEY_BROWSER_ARGS", "--modified-" + i);
                    Thread.sleep(10);
                }
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        });
        
        // Threads that resolve arguments concurrently
        for (int t = 0; t < threadCount; t++)
        {
            executor.submit(() -> {
                try
                {
                    for (int i = 0; i < 20; i++)
                    {
                        JourneyContext context = new JourneyContext();
                        ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                        
                        // Should not throw exceptions and should return valid results
                        assertNotNull(resolved);
                        assertNotNull(resolved.getArguments());
                        
                        Thread.sleep(5);
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
            fail("Environment variable race condition test failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }

    @Test
    public void providerStatelessness_sharedInstance() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider sharedProvider = new DefaultBrowserArgumentsProvider();
        
        int threadCount = 25;
        int operationsPerThread = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    for (int i = 0; i < operationsPerThread; i++)
                    {
                        final int operationId = i;
                        // Each thread uses different journey context and browser type
                        JourneyContext context = new JourneyContext();
                        context.getBrowserArguments().addGlobal(List.of("--unique-" + threadId + "-" + operationId));
                        
                        StandardBrowser browser = StandardBrowser.values()[operationId % StandardBrowser.values().length];
                        ResolvedBrowserArguments resolved = sharedProvider.resolve(browser, context);
                        
                        // Verify the resolution includes the unique argument
                        assertTrue(resolved.getArguments().stream()
                            .anyMatch(arg -> arg.equals("--unique-" + threadId + "-" + operationId)),
                            "Should contain unique argument for thread " + threadId + " operation " + operationId);
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
            fail("Provider statelessness test failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }

    @RepeatedTest(3)
    public void massiveParallelResolution() throws InterruptedException
    {
        DefaultBrowserArgumentsProvider provider = new DefaultBrowserArgumentsProvider();
        
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = Collections.synchronizedList(new java.util.ArrayList<>());
        
        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.submit(() -> {
                try
                {
                    JourneyContext context = new JourneyContext();
                    context.getBrowserArguments().addGlobal(List.of("--massive-test-" + threadId));
                    
                    ResolvedBrowserArguments resolved = provider.resolve(StandardBrowser.CHROME, context);
                    
                    assertNotNull(resolved);
                    assertTrue(resolved.getArguments().contains("--massive-test-" + threadId));
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
        
        assertTrue(latch.await(45, TimeUnit.SECONDS), "All threads should complete within 45 seconds");
        executor.shutdown();
        
        if (!exceptions.isEmpty())
        {
            fail("Massive parallel resolution failed: " + exceptions.get(0).getMessage(), exceptions.get(0));
        }
    }
}

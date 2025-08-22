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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IAsyncWebAction;
import io.github.jamoamo.webjourney.api.PageConsumerException;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserFactory;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserOptions;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import io.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import io.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException;
import io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException;
import org.apache.commons.lang3.function.FailableConsumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Action that navigates to multiple linked pages in parallel and consumes their data concurrently.
 * This is useful for scenarios where you need to fetch data from multiple related pages simultaneously.
 * 
 * @author James Amoore
 * @param <T> The type of the page class to be consumed from each URL.
 */
class ParallelNavigateAndConsumeAction<T> extends AWebAction implements IAsyncWebAction
{
    private final List<URL> urls;
    private final Class<T> pageClass;
    private final FailableConsumer<T, ? extends PageConsumerException> pageConsumer;
    private final int maxConcurrency;
    private final ExecutorService executor;
    private final IBrowserFactory browserFactory;
    
    /**
     * Creates a new ParallelNavigateAndConsumeAction with default concurrency settings.
     */
    ParallelNavigateAndConsumeAction(List<URL> urls, Class<T> pageClass, 
                                   FailableConsumer<T, ? extends PageConsumerException> pageConsumer,
                                   IBrowserFactory browserFactory)
    {
        this(urls, pageClass, pageConsumer, browserFactory, Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new ParallelNavigateAndConsumeAction with specified concurrency.
     */
    ParallelNavigateAndConsumeAction(List<URL> urls, Class<T> pageClass, 
                                   FailableConsumer<T, ? extends PageConsumerException> pageConsumer,
                                   IBrowserFactory browserFactory, int maxConcurrency)
    {
        if(urls == null || urls.isEmpty())
        {
            throw new IllegalArgumentException("URLs list cannot be null or empty.");
        }
        
        if(pageConsumer == null)
        {
            throw new NullPointerException("Page Consumer should not be null.");
        }
        
        if(pageClass == null)
        {
            throw new NullPointerException("Page Class should not be null.");
        }
        
        if(browserFactory == null)
        {
            throw new NullPointerException("Browser Factory should not be null.");
        }
        
        if(maxConcurrency <= 0)
        {
            throw new IllegalArgumentException("Max concurrency must be positive.");
        }
        
        this.urls = new ArrayList<>(urls);
        this.pageClass = pageClass;
        this.pageConsumer = pageConsumer;
        this.browserFactory = browserFactory;
        this.maxConcurrency = maxConcurrency;
        this.executor = Executors.newFixedThreadPool(maxConcurrency);
    }
    
    @Override
    protected ActionResult executeActionImpl(IJourneyContext context)
            throws BaseJourneyActionException
    {
        try
        {
            // Create a list of CompletableFutures for parallel execution
            List<CompletableFuture<Void>> futures = urls.stream()
                .map(url -> CompletableFuture.runAsync(() -> {
                    try
                    {
                        processUrl(url, context);
                    }
                    catch(Exception ex)
                    {
                        throw new RuntimeException("Failed to process URL: " + url, ex);
                    }
                }, executor))
                .collect(Collectors.toList());
            
            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            return ActionResult.SUCCESS;
        }
        catch(Exception ex)
        {
            throw new BaseJourneyActionException("Parallel navigation failed: " + ex.getMessage(), this, ex);
        }
        finally
        {
            executor.shutdown();
        }
    }
    
    private void processUrl(URL url, IJourneyContext context) throws Exception
    {
        // Create a new browser instance for this URL to avoid conflicts
        IBrowser browser = browserFactory.createBrowser(new DefaultBrowserOptions(), context);
        
        try
        {
            // Navigate to the URL
            browser.getActiveWindow().navigateTo(url.toString());
            
            // Wait for page to load (you might want to make this configurable)
            Thread.sleep(1000);
            
            // Consume the page
            EntityDefn entityDefn = new EntityDefn(this.pageClass);
            EntityCreator<T> creator = new EntityCreator(entityDefn, false, context.getJourneyObservers());
            T instance = creator.createNewEntity(browser);
            this.pageConsumer.accept(instance);
        }
        finally
        {
            if(browser != null)
            {
                browser.exit();
            }
        }
    }
    
    @Override
    public CompletableFuture<ActionResult> executeActionAsync(IJourneyContext context)
    {
        return CompletableFuture.supplyAsync(() -> {
            try
            {
                return executeActionImpl(context);
            }
            catch(BaseJourneyActionException ex)
            {
                throw new RuntimeException(ex);
            }
        }, executor);
    }
    
    @Override
    protected String getActionName()
    {
        return "Parallel Navigate and Consume (" + urls.size() + " URLs)";
    }
    
    @Override
    public boolean canRunInParallel()
    {
        return true;
    }
    
    /**
     * Get the maximum concurrency for this action.
     * @return the maximum number of concurrent operations
     */
    public int getMaxConcurrency()
    {
        return maxConcurrency;
    }
    
    /**
     * Get the number of URLs to process.
     * @return the number of URLs
     */
    public int getUrlCount()
    {
        return urls.size();
    }
    
    /**
     * Clean up resources.
     */
    public void shutdown()
    {
        if(!executor.isShutdown())
        {
            executor.shutdown();
        }
    }
}
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
import io.github.jamoamo.webjourney.reserved.entity.EntityCreator;
import io.github.jamoamo.webjourney.reserved.entity.EntityDefn;
import io.github.jamoamo.webjourney.reserved.entity.XEntityDefinitionException;
import io.github.jamoamo.webjourney.reserved.entity.XEntityFieldScrapeException;
import org.apache.commons.lang3.function.FailableConsumer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Action that consumes page data in parallel by extracting multiple field values concurrently.
 * This is useful for pages with many independent fields that can be extracted simultaneously.
 * 
 * @author James Amoore
 * @param <T> The type of the page class to be consumed.
 */
class ParallelConsumePageAction<T> extends AWebAction implements IAsyncWebAction
{
    private final Class<T> pageClass;
    private final FailableConsumer<T, ? extends PageConsumerException> pageConsumer;
    private final int maxConcurrency;
    private final ExecutorService executor;
    
    /**
     * Creates a new ParallelConsumePageAction with default concurrency settings.
     */
    ParallelConsumePageAction(Class<T> pageClass, FailableConsumer<T, ? extends PageConsumerException> pageConsumer)
    {
        this(pageClass, pageConsumer, Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new ParallelConsumePageAction with specified concurrency.
     */
    ParallelConsumePageAction(Class<T> pageClass, FailableConsumer<T, ? extends PageConsumerException> pageConsumer, int maxConcurrency)
    {
        if(pageConsumer == null)
        {
            throw new NullPointerException("Page Consumer should not be null.");
        }
        
        if(pageClass == null)
        {
            throw new NullPointerException("Page Class should not be null.");
        }
        
        if(maxConcurrency <= 0)
        {
            throw new IllegalArgumentException("Max concurrency must be positive.");
        }
        
        this.pageClass = pageClass;
        this.pageConsumer = pageConsumer;
        this.maxConcurrency = maxConcurrency;
        this.executor = Executors.newFixedThreadPool(maxConcurrency);
    }
    
    @Override
    protected ActionResult executeActionImpl(IJourneyContext context)
            throws BaseJourneyActionException
    {
        try
        {
            IBrowser browser = context.getBrowser();
            EntityDefn entityDefn = new EntityDefn(this.pageClass);
            EntityCreator<T> creator = new EntityCreator(entityDefn, false, context.getJourneyObservers());
            T instance = creator.createNewEntity(browser);
            this.pageConsumer.accept(instance);
        }
        catch(PageConsumerException | XEntityDefinitionException | XEntityFieldScrapeException ex)
        {
            throw new BaseJourneyActionException(ex.getMessage(), this, ex);
        }
        finally
        {
            executor.shutdown();
        }
        return ActionResult.SUCCESS;
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
        return "Parallel Consume Page";
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
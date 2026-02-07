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

import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IAsyncWebAction;
import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.JourneyBuilderException;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builder for creating journeys that can execute actions in parallel.
 * This builder analyzes action dependencies and executes independent actions concurrently
 * while respecting the dependency graph.
 * 
 * @author James Amoore
 */
public class ParallelJourneyBuilder implements IJourneyBuilder
{
    private final List<AWebAction> actions;
    private final Map<String, Set<String>> dependencies;
    private final int maxConcurrency;
    private final ExecutorService executor;
    
    /**
     * Creates a new ParallelJourneyBuilder with default concurrency settings.
     */
    public ParallelJourneyBuilder()
    {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Creates a new ParallelJourneyBuilder with specified concurrency.
     */
    public ParallelJourneyBuilder(int maxConcurrency)
    {
        if(maxConcurrency <= 0)
        {
            throw new IllegalArgumentException("Max concurrency must be positive.");
        }
        
        this.actions = new ArrayList<>();
        this.dependencies = new HashMap<>();
        this.maxConcurrency = maxConcurrency;
        this.executor = Executors.newFixedThreadPool(maxConcurrency);
    }
    
    /**
     * Add an action to the journey.
     * @param action the action to add
     * @return this builder
     */
    public ParallelJourneyBuilder addAction(AWebAction action)
    {
        if(action == null)
        {
            throw new IllegalArgumentException("Action cannot be null.");
        }
        
        this.actions.add(action);
        return this;
    }
    
    /**
     * Add a dependency between actions.
     * @param actionName the name of the action that depends on another
     * @param dependencyName the name of the action it depends on
     * @return this builder
     */
    public ParallelJourneyBuilder addDependency(String actionName, String dependencyName)
    {
        if(actionName == null || dependencyName == null)
        {
            throw new IllegalArgumentException("Action names cannot be null.");
        }
        
        dependencies.computeIfAbsent(actionName, k -> new HashSet<>()).add(dependencyName);
        return this;
    }
    
    /**
     * Build the parallel journey.
     * @return a journey that executes actions in parallel
     */
    public IJourney build()
    {
        return new ParallelJourney(actions, dependencies, maxConcurrency, executor);
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
    
    /**
     * Parallel journey implementation that executes actions respecting dependencies.
     */
    private static class ParallelJourney implements IJourney
    {
        private final List<AWebAction> actions;
        private final Map<String, Set<String>> dependencies;
        private final int maxConcurrency;
        private final ExecutorService executor;
        private final Map<String, AWebAction> actionMap;
        
        ParallelJourney(List<AWebAction> actions, Map<String, Set<String>> dependencies, 
                       int maxConcurrency, ExecutorService executor)
        {
            this.actions = new ArrayList<>(actions);
            this.dependencies = new HashMap<>(dependencies);
            this.maxConcurrency = maxConcurrency;
            this.executor = executor;
            this.actionMap = actions.stream()
                .collect(Collectors.toMap(AWebAction::getCrumbName, action -> action));
        }
        
        @Override
        public void doJourney(IJourneyContext context)
        {
            try
            {
                // Build dependency graph and find execution order
                List<List<AWebAction>> executionGroups = buildExecutionGroups();
                
                // Execute groups sequentially, but actions within each group in parallel
                for(List<AWebAction> group : executionGroups)
                {
                    executeActionGroup(group, context);
                }
            }
            catch(Exception ex)
            {
                throw new RuntimeException("Parallel journey execution failed", ex);
            }
        }
        
        private List<List<AWebAction>> buildExecutionGroups()
        {
            List<List<AWebAction>> groups = new ArrayList<>();
            Set<String> completed = new HashSet<>();
            Set<String> inProgress = new HashSet<>();
            
            while(completed.size() < actions.size())
            {
                List<AWebAction> readyActions = actions.stream()
                    .filter(action -> !completed.contains(action.getCrumbName()))
                    .filter(action -> !inProgress.contains(action.getCrumbName()))
                    .filter(action -> isReadyToExecute(action.getCrumbName(), completed))
                    .collect(Collectors.toList());
                
                if(readyActions.isEmpty())
                {
                    throw new IllegalStateException("Circular dependency detected in actions");
                }
                
                groups.add(readyActions);
                readyActions.forEach(action -> inProgress.add(action.getCrumbName()));
                
                // Mark actions as completed (we'll execute them in the next phase)
                completed.addAll(inProgress);
            }
            
            return groups;
        }
        
        private boolean isReadyToExecute(String actionName, Set<String> completed)
        {
            Set<String> deps = dependencies.get(actionName);
            if(deps == null || deps.isEmpty())
            {
                return true;
            }
            
            return deps.stream().allMatch(completed::contains);
        }
        
        private void executeActionGroup(List<AWebAction> group, IJourneyContext context)
        {
            if(group.size() == 1)
            {
                // Single action - execute synchronously
                executeAction(group.get(0), context);
            }
            else
            {
                // Multiple actions - execute in parallel
                List<CompletableFuture<Void>> futures = group.stream()
                    .map(action -> CompletableFuture.runAsync(() -> {
                        executeAction(action, context);
                    }, executor))
                    .collect(Collectors.toList());
                
                // Wait for all actions in this group to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        }
        
        private void executeAction(AWebAction action, IJourneyContext context)
        {
            try
            {
                if(action instanceof IAsyncWebAction && ((IAsyncWebAction) action).canRunInParallel())
                {
                    // Execute async action
                    IAsyncWebAction asyncAction = (IAsyncWebAction) action;
                    asyncAction.executeActionAsync(context).join();
                }
                else
                {
                    // Execute sync action
                    action.executeAction(context);
                }
            }
            catch(Exception ex)
            {
                throw new RuntimeException("Action execution failed: " + action.getCrumbName(), ex);
            }
        }
    }
    
    // Implement IJourneyBuilder methods (delegate to BaseJourneyBuilder for now)
    @Override
    public IJourneyBuilder navigateTo(String url) throws JourneyBuilderException
    {
        // This is a simplified implementation - in practice you'd want to integrate with BaseJourneyBuilder
        throw new UnsupportedOperationException("Navigation methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder navigateTo(java.net.URL url)
    {
        throw new UnsupportedOperationException("Navigation methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder back()
    {
        throw new UnsupportedOperationException("Navigation methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder forward()
    {
        throw new UnsupportedOperationException("Navigation methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder refresh()
    {
        throw new UnsupportedOperationException("Navigation methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder clickButton(String xPath)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder completeForm(Class<?> formClass, Object formData)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder consumePage(Class<?> pageClass, org.apache.commons.lang3.function.FailableConsumer<Object, ? extends io.github.jamoamo.webjourney.api.PageConsumerException> pageConsumer)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, IJourney subJourney)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, IJourneyBuilder subJourneyBuilder)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, org.apache.commons.lang3.function.FailableConsumer<io.github.jamoamo.webjourney.api.web.AElement, ? extends Exception> elementConsumer)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.web.AElement, ?, ? extends Exception> elementFunction)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, org.apache.commons.lang3.function.FailableConsumer<io.github.jamoamo.webjourney.api.web.AElement, ? extends Exception> elementConsumer, int maxIterations)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder repeatForChildElement(String parentXPath, String childXPath, org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.web.AElement, ?, ? extends Exception> elementFunction, int maxIterations)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder conditional(org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.IJourneyContext, Boolean, ? extends Exception> condition, IJourney trueJourney, IJourney falseJourney)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder conditional(org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.IJourneyContext, Boolean, ? extends Exception> condition, IJourneyBuilder trueJourneyBuilder, IJourneyBuilder falseJourneyBuilder)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder conditional(org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.IJourneyContext, Boolean, ? extends Exception> condition, IJourney trueJourney)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourneyBuilder conditional(org.apache.commons.lang3.function.FailableFunction<io.github.jamoamo.webjourney.api.IJourneyContext, Boolean, ? extends Exception> condition, IJourneyBuilder trueJourneyBuilder)
    {
        throw new UnsupportedOperationException("Action methods not yet implemented in ParallelJourneyBuilder");
    }
    
    @Override
    public IJourney build()
    {
        return build();
    }
}
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
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.ActionResult;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for asynchronous web actions that can be executed in parallel.
 * Extends the synchronous AWebAction with async capabilities.
 * 
 * @author James Amoore
 */
public interface IAsyncWebAction extends AWebAction
{
    /**
     * Execute the action asynchronously.
     * @param context the journey context
     * @return a CompletableFuture containing the action result
     */
    CompletableFuture<ActionResult> executeActionAsync(IJourneyContext context);
    
    /**
     * Check if this action can run in parallel with other actions.
     * Actions that modify the same page state should return false.
     * @return true if the action can run in parallel
     */
    default boolean canRunInParallel() { return false; }
    
    /**
     * Get the dependencies for this action.
     * Actions that depend on the result of other actions should specify their dependencies.
     * @return set of action names this action depends on
     */
    default java.util.Set<String> getDependencies() { return java.util.Collections.emptySet(); }
    
    /**
     * Get the priority for parallel execution.
     * Lower numbers have higher priority.
     * @return priority value (default: 0)
     */
    default int getParallelPriority() { return 0; }
}
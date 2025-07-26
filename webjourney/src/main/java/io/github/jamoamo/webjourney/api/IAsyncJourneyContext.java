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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.ActionResult;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserPool;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Enhanced journey context that supports asynchronous operations and browser pool access.
 * 
 * <p>
 * The AsyncJourneyContext extends the standard IJourneyContext to provide additional capabilities
 * for asynchronous journey execution, including browser pool management and async action execution.
 * This interface enables non-blocking operations and better resource utilization in concurrent
 * journey execution scenarios.
 * </p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 * <li>Browser pool access for resource sharing across concurrent journeys</li>
 * <li>Asynchronous action execution with CompletableFuture support</li>
 * <li>ExecutorService integration for custom thread pool management</li>
 * <li>Backward compatibility with existing synchronous operations</li>
 * </ul>
 * 
 * <h2>Basic Usage:</h2>
 * 
 * <pre>{@code
 * AsyncJourneyContext context = new DefaultAsyncJourneyContext(browserPool, executorService);
 * 
 * // Execute an action asynchronously
 * CompletableFuture<ActionResult> future = context.executeActionAsync(action);
 * future.thenAccept(result -> {
 *     if (result == ActionResult.SUCCESS) {
 *         System.out.println("Action completed successfully");
 *     }
 * });
 * 
 * // Acquire a browser from the pool
 * CompletableFuture<IBrowser> browserFuture = context.acquireBrowser();
 * browserFuture.thenAccept(browser -> {
 *     try {
 *         // Use the browser
 *         browser.getActiveWindow().navigateToUrl("https://example.com");
 *     } finally {
 *         context.releaseBrowser(browser);
 *     }
 * });
 * }</pre>
 * 
 * <h2>Integration with Existing Code:</h2>
 * 
 * <p>
 * This interface maintains full backward compatibility with existing synchronous code. All methods
 * from IJourneyContext are available, and the async capabilities are additive.
 * </p>
 * 
 * @author James Amoore
 * @see IJourneyContext
 * @see IBrowserPool
 * @see AWebAction
 * @since 1.1.0
 */
public interface IAsyncJourneyContext extends IJourneyContext
{
	/**
	 * Executes a web action asynchronously.
	 * 
	 * <p>
	 * This method provides non-blocking execution of web actions, allowing for better resource
	 * utilization and concurrent processing. The action is executed using the context's
	 * ExecutorService, and the result is returned as a CompletableFuture.
	 * </p>
	 * 
	 * @param action The web action to execute asynchronously
	 * @return A CompletableFuture that completes with the action result
	 * @throws IllegalArgumentException if action is null
	 * @throws IllegalStateException if the context has been shut down
	 * @since 1.1.0
	 */
	CompletableFuture<ActionResult> executeActionAsync(AWebAction action);
	
	/**
	 * Returns the browser pool associated with this context.
	 * 
	 * <p>
	 * The browser pool provides managed access to browser instances, enabling resource sharing
	 * across multiple concurrent journeys. This allows for better resource utilization and
	 * controlled browser lifecycle management.
	 * </p>
	 * 
	 * @return The browser pool for this context
	 * @since 1.1.0
	 */
	IBrowserPool getBrowserPool();
	
	/**
	 * Returns the ExecutorService used for asynchronous operations.
	 * 
	 * <p>
	 * This ExecutorService is used for executing async actions and managing the thread pool
	 * for concurrent operations. It can be configured to control the level of concurrency
	 * and resource usage.
	 * </p>
	 * 
	 * @return The ExecutorService for async operations
	 * @since 1.1.0
	 */
	ExecutorService getActionExecutor();
	
	/**
	 * Acquires a browser instance from the browser pool asynchronously.
	 * 
	 * <p>
	 * This method provides non-blocking access to browser instances from the pool. The returned
	 * CompletableFuture will complete when a browser becomes available. It's important to
	 * release the browser back to the pool when it's no longer needed.
	 * </p>
	 * 
	 * @return A CompletableFuture that completes with an available browser instance
	 * @throws IllegalStateException if the browser pool has been shut down
	 * @since 1.1.0
	 */
	CompletableFuture<IBrowser> acquireBrowser();
	
	/**
	 * Releases a browser instance back to the browser pool.
	 * 
	 * <p>
	 * This method should be called when a browser is no longer needed. The browser will be
	 * returned to the pool for reuse by other threads. If the browser is in an invalid state
	 * or the pool is at maximum capacity, the browser will be closed and discarded.
	 * </p>
	 * 
	 * @param browser The browser instance to release
	 * @throws IllegalArgumentException if browser is null
	 * @since 1.1.0
	 */
	void releaseBrowser(IBrowser browser);
	
	/**
	 * Sets the browser instance for this context.
	 * 
	 * <p>
	 * This method allows setting a specific browser instance for this context, typically
	 * acquired from the browser pool. This is useful when you want to use a specific browser
	 * instance for the entire journey execution.
	 * </p>
	 * 
	 * @param browser The browser instance to set
	 * @throws IllegalArgumentException if browser is null
	 * @since 1.1.0
	 */
	void setBrowser(IBrowser browser);
} 

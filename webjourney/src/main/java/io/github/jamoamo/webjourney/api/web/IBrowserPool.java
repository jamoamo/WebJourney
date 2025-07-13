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
package io.github.jamoamo.webjourney.api.web;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A managed pool of browser instances to enable resource sharing across concurrent journeys.
 * 
 * <p>
 * The BrowserPool provides thread-safe acquisition and release of browser instances, allowing multiple
 * concurrent journeys to share a limited number of browser resources. This helps manage memory usage
 * and system resources while enabling parallel execution.
 * </p>
 * 
 * <h2>Basic Usage:</h2>
 * 
 * <pre>{@code
 * BrowserPool pool = new DefaultBrowserPool(browserFactory, options, 5, 10);
 * 
 * // Acquire a browser
 * IBrowser browser = pool.acquireBrowser();
 * try {
 *     // Use the browser for journey execution
 *     browser.getActiveWindow().navigateToUrl("https://example.com");
 * } finally {
 *     // Always release the browser back to the pool
 *     pool.releaseBrowser(browser);
 * }
 * }</pre>
 * 
 * <h2>Async Usage:</h2>
 * 
 * <pre>{@code
 * // Acquire browser asynchronously
 * CompletableFuture<IBrowser> future = pool.acquireBrowserAsync();
 * future.thenAccept(browser -> {
 *     try {
 *         // Use browser
 *     } finally {
 *         pool.releaseBrowser(browser);
 *     }
 * });
 * }</pre>
 * 
 * @author James Amoore
 * @see IBrowser
 * @see IBrowserFactory
 * @see IBrowserOptions
 * @since 1.1.0
 */
public interface IBrowserPool
{
	/**
	 * Acquires a browser instance from the pool.
	 * 
	 * <p>
	 * This method blocks until a browser becomes available. If the pool is at maximum capacity and all
	 * browsers are in use, this method will wait until a browser is released back to the pool.
	 * </p>
	 * 
	 * @return A browser instance ready for use
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 * @throws IllegalStateException if the pool has been shut down
	 * @since 1.1.0
	 */
	IBrowser acquireBrowser() throws InterruptedException;

	/**
	 * Acquires a browser instance from the pool with a timeout.
	 * 
	 * <p>
	 * This method blocks until a browser becomes available or the timeout expires. If the pool is at
	 * maximum capacity and all browsers are in use, this method will wait up to the specified timeout
	 * for a browser to become available.
	 * </p>
	 * 
	 * @param timeout The maximum time to wait for a browser
	 * @param unit    The time unit of the timeout parameter
	 * @return A browser instance ready for use, or null if timeout expired
	 * @throws InterruptedException if the current thread is interrupted while waiting
	 * @throws IllegalArgumentException if timeout is negative
	 * @throws IllegalStateException if the pool has been shut down
	 * @since 1.1.0
	 */
	IBrowser acquireBrowser(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Acquires a browser instance from the pool asynchronously.
	 * 
	 * <p>
	 * This method returns immediately with a CompletableFuture that will complete when a browser
	 * becomes available. The future may complete exceptionally if the pool is shut down or an error
	 * occurs during browser creation.
	 * </p>
	 * 
	 * @return A CompletableFuture that completes with a browser instance when available
	 * @throws IllegalStateException if the pool has been shut down
	 * @since 1.1.0
	 */
	CompletableFuture<IBrowser> acquireBrowserAsync();

	/**
	 * Releases a browser instance back to the pool.
	 * 
	 * <p>
	 * This method should be called when a browser is no longer needed. The browser will be returned
	 * to the pool for reuse by other threads. If the browser is in an invalid state or the pool is
	 * at maximum capacity, the browser will be closed and discarded.
	 * </p>
	 * 
	 * @param browser The browser instance to release
	 * @throws IllegalArgumentException if browser is null
	 * @since 1.1.0
	 */
	void releaseBrowser(IBrowser browser);

	/**
	 * Returns the current pool statistics.
	 * 
	 * @return Pool statistics including current size, available browsers, and usage metrics
	 * @since 1.1.0
	 */
	PoolStatistics getStatistics();

	/**
	 * Shuts down the browser pool and closes all browser instances.
	 * 
	 * <p>
	 * This method initiates an orderly shutdown of the pool. All browser instances will be closed,
	 * and no new browsers will be created. Any threads waiting to acquire browsers will be interrupted.
	 * </p>
	 * 
	 * @since 1.1.0
	 */
	void shutdown();

	/**
	 * Returns true if this pool has been shut down.
	 * 
	 * @return true if this pool has been shut down
	 * @since 1.1.0
	 */
	boolean isShutdown();
} 

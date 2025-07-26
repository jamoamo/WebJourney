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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Interface for asynchronous journey execution with CompletableFuture support.
 * 
 * <p>
 * The AsyncJourneyExecutor provides non-blocking execution of web journeys, allowing multiple journeys to be executed
 * concurrently. This interface supports both single journey execution and future extensions for parallel execution.
 * </p>
 * 
 * <h2>Basic Usage:</h2>
 * 
 * <pre>{@code
 * AsyncJourneyExecutor executor = new DefaultAsyncJourneyExecutor();
 * 
 * // Execute a single journey asynchronously
 * CompletableFuture<Void> future = executor.executeJourneyAsync(journey, travelOptions);
 * 
 * // Handle completion
 * future.thenRun(() -> System.out.println("Journey completed")).exceptionally(throwable ->
 * {
 * 	System.err.println("Journey failed: " + throwable.getMessage());
 * 	return null;
 * });
 * }</pre>
 * 
 * <h2>Timeout Support:</h2>
 * 
 * <pre>{@code
 * // Execute with timeout
 * CompletableFuture<Void> future = executor.executeJourneyAsync(journey, travelOptions, 30, TimeUnit.SECONDS);
 * }</pre>
 * 
 * @author James Amoore
 * @see IJourney
 * @see ITravelOptions
 * @since 1.1.0
 */
public interface IAsyncJourneyExecutor
{
	/**
	 * Executes a single journey asynchronously.
	 * 
	 * @param journey       The journey to execute
	 * @param travelOptions The travel options for the journey execution
	 * @return A CompletableFuture that completes when the journey finishes
	 * @throws IllegalArgumentException if journey or travelOptions is null
	 * @since 1.1.0
	 */
	CompletableFuture<Void> executeJourneyAsync(IJourney journey, ITravelOptions travelOptions);

	/**
	 * Executes a single journey asynchronously with a timeout.
	 * 
	 * @param journey       The journey to execute
	 * @param travelOptions The travel options for the journey execution
	 * @param timeout       The maximum time to wait for completion
	 * @param unit          The time unit of the timeout parameter
	 * @return A CompletableFuture that completes when the journey finishes or times out
	 * @throws IllegalArgumentException if journey or travelOptions is null, or if timeout is negative
	 * @since 1.1.0
	 */
	CompletableFuture<Void> executeJourneyAsync(IJourney journey, ITravelOptions travelOptions, long timeout,
			TimeUnit unit);

	/**
	 * Cancels all currently running journeys and shuts down the executor.
	 * 
	 * <p>
	 * This method initiates an orderly shutdown in which previously submitted journeys are executed, but no new journeys
	 * will be accepted. This method does not wait for previously submitted journeys to complete execution.
	 * </p>
	 * 
	 * @since 1.1.0
	 */
	void shutdown();

	/**
	 * Attempts to stop all actively executing journeys and halts the processing of waiting journeys.
	 * 
	 * <p>
	 * This method does not wait for actively executing journeys to terminate. There are no guarantees beyond best-effort
	 * attempts to stop processing actively executing journeys.
	 * </p>
	 * 
	 * @since 1.1.0
	 */
	void shutdownNow();

	/**
	 * Returns true if this executor has been shut down.
	 * 
	 * @return true if this executor has been shut down
	 * @since 1.1.0
	 */
	boolean isShutdown();

	/**
	 * Returns true if all journeys have completed following shut down.
	 * 
	 * @return true if all journeys have completed following shut down
	 * @since 1.1.0
	 */
	boolean isTerminated();
}

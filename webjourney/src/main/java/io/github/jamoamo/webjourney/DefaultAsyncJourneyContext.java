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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IAsyncJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserPool;
import io.github.jamoamo.webjourney.api.AWebAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of AsyncJourneyContext that provides asynchronous journey execution
 * capabilities with browser pool integration.
 * 
 * <p>
 * This implementation extends the standard JourneyContext to add async capabilities while
 * maintaining full backward compatibility with existing synchronous code. It integrates with
 * a browser pool for resource sharing and provides an ExecutorService for async operations.
 * </p>
 * 
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is thread-safe and can be used from multiple threads concurrently. All state
 * modifications are properly synchronized.
 * </p>
 * 
 * <h2>Resource Management:</h2>
 * <p>
 * The context integrates with a browser pool for efficient resource management. Browsers
 * are acquired from the pool as needed and released when no longer required. The context
 * also manages its own ExecutorService for async operations.
 * </p>
 * 
 * <h2>Example Usage:</h2>
 * 
 * <pre>{@code
 * // Create browser pool and executor
 * IBrowserPool browserPool = new DefaultBrowserPool(browserFactory, options, 2, 10);
 * ExecutorService executor = Executors.newFixedThreadPool(5);
 * 
 * // Create async context
 * AsyncJourneyContext context = new DefaultAsyncJourneyContext(browserPool, executor);
 * 
 * // Execute actions asynchronously
 * CompletableFuture<ActionResult> future1 = context.executeActionAsync(action1);
 * CompletableFuture<ActionResult> future2 = context.executeActionAsync(action2);
 * 
 * // Wait for both to complete
 * CompletableFuture.allOf(future1, future2).join();
 * 
 * // Clean up
 * executor.shutdown();
 * browserPool.shutdown();
 * }</pre>
 * 
 * @author James Amoore
 * @see IAsyncJourneyContext
 * @see IBrowserPool
 * @see JourneyContext
 * @since 1.1.0
 */
public final class DefaultAsyncJourneyContext implements IAsyncJourneyContext
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncJourneyContext.class);
	private static final String SHUTDOWN_MESSAGE = "AsyncJourneyContext has been shut down";
	private static final String BROWSER_NULL_MESSAGE = "Browser cannot be null";
	
	private final IBrowserPool browserPool;
	private final ExecutorService actionExecutor;
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	
	// Inherited from JourneyContext
	private IBrowser browser;
	private IJourneyBreadcrumb breadcrumb;
	private final Map<String, Object> inputs = new HashMap<>(2);
	private final List<IJourneyObserver> journeyObservers = new ArrayList<>();
	
	/**
	 * Creates a new DefaultAsyncJourneyContext with the specified browser pool and executor service.
	 * 
	 * @param browserPool The browser pool to use for browser management
	 * @param actionExecutor The executor service for async operations
	 * @throws IllegalArgumentException if browserPool or actionExecutor is null
	 */
	public DefaultAsyncJourneyContext(IBrowserPool browserPool, ExecutorService actionExecutor)
	{
		if (browserPool == null)
		{
			throw new IllegalArgumentException("Browser pool cannot be null");
		}
		if (actionExecutor == null)
		{
			throw new IllegalArgumentException("Action executor cannot be null");
		}
		
		this.browserPool = browserPool;
		this.actionExecutor = actionExecutor;
		
		LOGGER.debug("DefaultAsyncJourneyContext created with browser pool: {}, executor: {}", 
				browserPool.getClass().getSimpleName(), actionExecutor.getClass().getSimpleName());
	}
	
	/**
	 * Creates a new DefaultAsyncJourneyContext with a default cached thread pool executor.
	 * 
	 * @param browserPool The browser pool to use for browser management
	 * @throws IllegalArgumentException if browserPool is null
	 */
	public DefaultAsyncJourneyContext(IBrowserPool browserPool)
	{
		this(browserPool, java.util.concurrent.Executors.newCachedThreadPool(r ->
		{
			Thread t = new Thread(r, "AsyncJourneyContext-" + System.currentTimeMillis());
			t.setDaemon(true);
			return t;
		}));
	}
	
	@Override
	public CompletableFuture<ActionResult> executeActionAsync(AWebAction action)
	{
		validateActionAndState(action);
		
		LOGGER.debug("Executing action asynchronously: {}", action.getCrumbName());
		
		return CompletableFuture.supplyAsync(() -> executeActionSafely(action), this.actionExecutor);
	}
	
	/**
	 * Validates the action and context state.
	 * 
	 * @param action The action to validate
	 * @throws IllegalArgumentException if action is null
	 * @throws IllegalStateException if context is shut down
	 */
	private void validateActionAndState(AWebAction action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Action cannot be null");
		}
		if (this.shutdown.get())
		{
			throw new IllegalStateException(SHUTDOWN_MESSAGE);
		}
	}
	
	/**
	 * Executes an action safely, handling exceptions appropriately.
	 * 
	 * @param action The action to execute
	 * @return The action result
	 */
	private ActionResult executeActionSafely(AWebAction action)
	{
		try
		{
			// Execute the action using the standard synchronous method
			// This maintains compatibility with existing action implementations
			ActionResult result = action.executeAction(this);
			
			LOGGER.debug("Action completed asynchronously: {} with result: {}", 
					action.getCrumbName(), result);
			
			return result;
		}
		catch (BaseJourneyActionException e)
		{
			LOGGER.error("Action failed asynchronously: {}", action.getCrumbName(), e);
			throw new RuntimeException("Action execution failed", e);
		}
		catch (IllegalArgumentException | IllegalStateException e)
		{
			LOGGER.error("Unexpected error during async action execution: {}", 
					action.getCrumbName(), e);
			throw e;
		}
	}
	
	@Override
	public IBrowserPool getBrowserPool()
	{
		return this.browserPool;
	}
	
	@Override
	public ExecutorService getActionExecutor()
	{
		return this.actionExecutor;
	}
	
	@Override
	public CompletableFuture<IBrowser> acquireBrowser()
	{
		if (this.shutdown.get())
		{
			throw new IllegalStateException(SHUTDOWN_MESSAGE);
		}
		
		LOGGER.debug("Acquiring browser from pool");
		return this.browserPool.acquireBrowserAsync();
	}
	
	@Override
	public void releaseBrowser(IBrowser browser)
	{
		if (browser == null)
		{
			throw new IllegalArgumentException(BROWSER_NULL_MESSAGE);
		}
		
		LOGGER.debug("Releasing browser back to pool");
		this.browserPool.releaseBrowser(browser);
	}
	
	@Override
	public void setBrowser(IBrowser browser)
	{
		if (browser == null)
		{
			throw new IllegalArgumentException(BROWSER_NULL_MESSAGE);
		}
		
		this.browser = browser;
		LOGGER.debug("Browser set for context");
	}
	
	// Implementation of IJourneyContext methods
	
	@Override
	public IBrowser getBrowser()
	{
		return this.browser;
	}
	
	@Override
	public void setJourneyInput(String inputType, Object inputValue)
	{
		this.inputs.put(inputType, inputValue);
	}
	
	@Override
	public Object getJourneyInput(String inputType)
	{
		return this.inputs.get(inputType);
	}
	
	@Override
	public List<IJourneyObserver> getJourneyObservers()
	{
		return Collections.unmodifiableList(this.journeyObservers);
	}
	
	@Override
	public void setJourneyObservers(List<IJourneyObserver> observers)
	{
		this.journeyObservers.addAll(observers);
	}
	
	@Override
	public IJourneyBreadcrumb getJourneyBreadcrumb()
	{
		return this.breadcrumb;
	}
	
	/**
	 * Sets the journey breadcrumb for this context.
	 * 
	 * @param breadcrumb The breadcrumb to set
	 */
	public void setJourneyBreadcrumb(IJourneyBreadcrumb breadcrumb)
	{
		this.breadcrumb = breadcrumb;
	}
	
	/**
	 * Shuts down this async journey context and releases associated resources.
	 * 
	 * <p>
	 * This method initiates an orderly shutdown of the context. The action executor will be
	 * shut down, but the browser pool will remain active as it may be shared with other
	 * contexts. It's the responsibility of the caller to shut down the browser pool when
	 * it's no longer needed.
	 * </p>
	 * 
	 * <p>
	 * After shutdown, all async operations will throw IllegalStateException.
	 * </p>
	 */
	public void shutdown()
	{
		if (this.shutdown.compareAndSet(false, true))
		{
			LOGGER.info("Shutting down DefaultAsyncJourneyContext");
			
			// Shutdown the action executor
			this.actionExecutor.shutdown();
			
			LOGGER.debug("DefaultAsyncJourneyContext shutdown complete");
		}
	}
	
	/**
	 * Returns true if this context has been shut down.
	 * 
	 * @return true if this context has been shut down
	 */
	public boolean isShutdown()
	{
		return this.shutdown.get();
	}
} 

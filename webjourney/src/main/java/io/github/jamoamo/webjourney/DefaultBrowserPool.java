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

import io.github.jamoamo.webjourney.api.web.IBrowserPool;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserFactory;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.web.PoolStatistics;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of BrowserPool with thread-safe acquire/release functionality.
 * 
 * <p>
 * This implementation provides a managed pool of browser instances with configurable minimum and maximum
 * pool sizes. It ensures thread-safe operations and proper resource lifecycle management.
 * </p>
 * 
 * <h2>Features:</h2>
 * <ul>
 * <li>Thread-safe browser acquisition and release</li>
 * <li>Configurable minimum and maximum pool sizes</li>
 * <li>Automatic browser creation up to minimum size</li>
 * <li>Comprehensive statistics tracking</li>
 * <li>Graceful shutdown with resource cleanup</li>
 * <li>Async browser acquisition support</li>
 * </ul>
 * 
 * @author James Amoore
 * @see IBrowserPool
 * @see IBrowser
 * @see IBrowserFactory
 * @since 1.1.0
 */
public final class DefaultBrowserPool implements IBrowserPool
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBrowserPool.class);
	private static final String SHUTDOWN_MESSAGE = "Browser pool has been shut down";

	private final IBrowserFactory browserFactory;
	private final IBrowserOptions browserOptions;
	private final int minSize;
	private final int maxSize;
	
	private final BlockingQueue<IBrowser> availableBrowsers;
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	private final ExecutorService asyncExecutor;
	
	// Statistics tracking
	private final AtomicLong totalAcquisitions = new AtomicLong(0);
	private final AtomicLong totalReleases = new AtomicLong(0);
	private final AtomicLong failedAcquisitions = new AtomicLong(0);
	private final AtomicLong totalAcquisitionTimeNanos = new AtomicLong(0);
	private final AtomicLong maxAcquisitionTimeNanos = new AtomicLong(0);
	private final AtomicReference<Instant> lastAcquisitionTime = new AtomicReference<>();
	private final AtomicReference<Instant> lastReleaseTime = new AtomicReference<>();
	
	// Track in-use browsers separately
	private final AtomicLong inUseBrowsers = new AtomicLong(0);
	
	// Track total browsers created
	private final AtomicLong totalBrowsersCreated = new AtomicLong(0);
	
	// Lock for browser creation to prevent race conditions
	private final Object browserCreationLock = new Object();
	
	/**
	 * Creates a new DefaultBrowserPool with the specified configuration.
	 * 
	 * @param browserFactory The factory to create new browser instances
	 * @param browserOptions The options to use when creating browsers
	 * @param minSize        The minimum number of browsers to maintain in the pool
	 * @param maxSize        The maximum number of browsers allowed in the pool
	 * @throws IllegalArgumentException if minSize is negative, maxSize is less than minSize, or either parameter is null
	 */
	public DefaultBrowserPool(IBrowserFactory browserFactory, IBrowserOptions browserOptions, int minSize, int maxSize)
	{
		if (browserFactory == null)
		{
			throw new IllegalArgumentException("Browser factory cannot be null");
		}
		if (browserOptions == null)
		{
			throw new IllegalArgumentException("Browser options cannot be null");
		}
		if (minSize < 0)
		{
			throw new IllegalArgumentException("Minimum size cannot be negative");
		}
		if (maxSize < minSize)
		{
			throw new IllegalArgumentException("Maximum size cannot be less than minimum size");
		}
		
		this.browserFactory = browserFactory;
		this.browserOptions = browserOptions;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.availableBrowsers = new ArrayBlockingQueue<>(maxSize);
		this.asyncExecutor = Executors.newCachedThreadPool();
		
		// Initialize the pool with minimum number of browsers
		initializePool();
		
		LOGGER.info("Browser pool initialized with minSize={}, maxSize={}", minSize, maxSize);
	}
	
	/**
	 * Creates a new DefaultBrowserPool with default configuration.
	 * 
	 * @param browserFactory The factory to create new browser instances
	 * @param browserOptions The options to use when creating browsers
	 */
	@SuppressWarnings("checkstyle:MagicNumber")
	public DefaultBrowserPool(IBrowserFactory browserFactory, IBrowserOptions browserOptions)
	{
		this(browserFactory, browserOptions, 2, 10);
	}
	
	@SuppressWarnings({"checkstyle:MethodLength", "checkstyle:IllegalCatch"})
	@Override
	public IBrowser acquireBrowser() throws InterruptedException
	{
		if (this.shutdown.get())
		{
			throw new IllegalStateException(SHUTDOWN_MESSAGE);
		}
		
		long startTime = System.nanoTime();
		IBrowser browser = null;
		
		try
		{
			// Try to get an available browser
			browser = this.availableBrowsers.poll();
			
			// If no browser available and we haven't reached max size, create a new one
			if (browser == null)
			{
				synchronized (this.browserCreationLock)
				{
					// Check pool size inside synchronized block to prevent race conditions
					if (getCurrentPoolSize() < this.maxSize)
					{
						browser = createNewBrowser();
					}
				}
			}
			
			// If still no browser, wait for one to become available
			if (browser == null)
			{
				browser = this.availableBrowsers.take();
			}
			
			// Increment in-use counter
			this.inUseBrowsers.incrementAndGet();
			
			// Update statistics
			long acquisitionTime = System.nanoTime() - startTime;
			updateAcquisitionStatistics(acquisitionTime);
			
			LOGGER.debug("Acquired browser from pool. Available: {}, In use: {}, Total pool size: {}", 
					this.availableBrowsers.size(), this.inUseBrowsers.get(), getCurrentPoolSize());
			
			return browser;
		}
		catch (Exception e)
		{
			this.failedAcquisitions.incrementAndGet();
			LOGGER.error("Failed to acquire browser from pool", e);
			throw e;
		}
	}
	
	@SuppressWarnings({"checkstyle:MethodLength", "checkstyle:IllegalCatch"})
	@Override
	public IBrowser acquireBrowser(long timeout, TimeUnit unit) throws InterruptedException
	{
		if (timeout < 0)
		{
			throw new IllegalArgumentException("Timeout cannot be negative");
		}
		if (unit == null)
		{
			throw new IllegalArgumentException("Time unit cannot be null");
		}
		if (this.shutdown.get())
		{
			throw new IllegalStateException(SHUTDOWN_MESSAGE);
		}
		
		long startTime = System.nanoTime();
		IBrowser browser = null;
		
		try
		{
			// Try to get an available browser
			browser = this.availableBrowsers.poll();
			
			// If no browser available and we haven't reached max size, create a new one
			if (browser == null)
			{
				synchronized (this.browserCreationLock)
				{
					// Check pool size inside synchronized block to prevent race conditions
					if (getCurrentPoolSize() < this.maxSize)
					{
						browser = createNewBrowser();
						LOGGER.debug("Created new browser. Current pool size: {}", getCurrentPoolSize());
					}
				}
			}
			
			// If still no browser, wait for one to become available with timeout
			if (browser == null)
			{
				LOGGER.debug("No browser available, waiting with timeout. Available: {}, In use: {}, Max: {}", 
						this.availableBrowsers.size(), this.inUseBrowsers.get(), this.maxSize);
				browser = this.availableBrowsers.poll(timeout, unit);
			}
			
			if (browser != null)
			{
				// Increment in-use counter
				this.inUseBrowsers.incrementAndGet();
				
				// Update statistics
				long acquisitionTime = System.nanoTime() - startTime;
				updateAcquisitionStatistics(acquisitionTime);
				
				LOGGER.debug("Acquired browser from pool with timeout. Available: {}, In use: {}, Total pool size: {}", 
						this.availableBrowsers.size(), this.inUseBrowsers.get(), getCurrentPoolSize());
			}
			else
			{
				this.failedAcquisitions.incrementAndGet();
				LOGGER.warn("Timeout waiting for browser acquisition. Available: {}, In use: {}, Total pool size: {}", 
						this.availableBrowsers.size(), this.inUseBrowsers.get(), getCurrentPoolSize());
			}
			
			return browser;
		}
		catch (Exception e)
		{
			this.failedAcquisitions.incrementAndGet();
			LOGGER.error("Failed to acquire browser from pool with timeout", e);
			throw e;
		}
	}
	
	@Override
	public CompletableFuture<IBrowser> acquireBrowserAsync()
	{
		if (this.shutdown.get())
		{
			throw new IllegalStateException(SHUTDOWN_MESSAGE);
		}
		
		return CompletableFuture.supplyAsync(() -> 
		{
			try
			{
				return acquireBrowser();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				throw new RuntimeException("Interrupted while acquiring browser asynchronously", e);
			}
		}, this.asyncExecutor);
	}
	
	@Override
	@SuppressWarnings("checkstyle:MethodLength")
	public void releaseBrowser(IBrowser browser)
	{
		if (browser == null)
		{
			throw new IllegalArgumentException("Browser cannot be null");
		}
		
		if (this.shutdown.get())
		{
			// If pool is shut down, just close the browser
			closeBrowser(browser);
			return;
		}
		
		// Decrement in-use counter
		this.inUseBrowsers.decrementAndGet();
	
		// Update release statistics
		this.totalReleases.incrementAndGet();
		this.lastReleaseTime.set(Instant.now());
		
		// Try to return the browser to the pool
		boolean returned = this.availableBrowsers.offer(browser);
		
		if (returned)
		{
			LOGGER.debug("Released browser back to pool. Available: {}, In use: {}, Total pool size: {}", 
					this.availableBrowsers.size(), this.inUseBrowsers.get(), getCurrentPoolSize());
		}
		else
		{
			// Pool is full, close the browser
			LOGGER.debug("Pool is full, closing released browser. Available: {}, In use: {}, Total pool size: {}", 
					this.availableBrowsers.size(), this.inUseBrowsers.get(), getCurrentPoolSize());
			closeBrowser(browser);
		}
	}
	
	@Override
	public PoolStatistics getStatistics()
	{
		int currentSize = getCurrentPoolSize();
		int available = this.availableBrowsers.size();
		int inUse = (int) this.inUseBrowsers.get();
		
		Duration avgAcquisitionTime = Duration.ZERO;
		if (this.totalAcquisitions.get() > 0)
		{
			avgAcquisitionTime = Duration.ofNanos(this.totalAcquisitionTimeNanos.get() / this.totalAcquisitions.get());
		}
		
		Duration maxAcquisitionTime = Duration.ofNanos(this.maxAcquisitionTimeNanos.get());
		
		return new PoolStatistics(
				currentSize, available, inUse, this.minSize, this.maxSize,
				this.totalAcquisitions.get(), this.totalReleases.get(), this.failedAcquisitions.get(),
				avgAcquisitionTime, maxAcquisitionTime,
				this.lastAcquisitionTime.get(), this.lastReleaseTime.get(),
				this.shutdown.get());
	}
	
	@Override
	@SuppressWarnings("checkstyle:MagicNumber")
	public void shutdown()
	{
		if (this.shutdown.compareAndSet(false, true))
		{
			LOGGER.info("Shutting down browser pool");
			
			// Shutdown async executor
			this.asyncExecutor.shutdown();
			try
			{
				if (!this.asyncExecutor.awaitTermination(5, TimeUnit.SECONDS))
				{
					this.asyncExecutor.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				this.asyncExecutor.shutdownNow();
				Thread.currentThread().interrupt();
			}
			
			// Close all browsers in the pool
			IBrowser browser;
			while ((browser = this.availableBrowsers.poll()) != null)
			{
				closeBrowser(browser);
			}
			
			LOGGER.info("Browser pool shutdown complete");
		}
	}
	
	@Override
	public boolean isShutdown()
	{
		return this.shutdown.get();
	}
	
	/**
	 * Initializes the pool with the minimum number of browsers.
	 */
	@SuppressWarnings("checkstyle:IllegalCatch")
	private void initializePool()
	{
		for (int i = 0; i < this.minSize; i++)
		{
			try
			{
				IBrowser browser = createNewBrowser();
				this.availableBrowsers.offer(browser);
			}
			catch (Exception e)
			{
				LOGGER.error("Failed to create browser during pool initialization", e);
			}
		}
		LOGGER.debug("Pool initialized with {} browsers", this.totalBrowsersCreated.get());
	}
	
	/**
	 * Creates a new browser instance.
	 * 
	 * @return A new browser instance
	 * @throws RuntimeException if browser creation fails
	 */
	@SuppressWarnings("checkstyle:IllegalCatch")
	private IBrowser createNewBrowser()
	{
		try
		{
			LOGGER.debug("Creating new browser. Current pool size: {}, In use: {}, Max: {}", 
					getCurrentPoolSize(), this.inUseBrowsers.get(), this.maxSize);
			IBrowser browser = this.browserFactory.createBrowser(this.browserOptions);
			this.totalBrowsersCreated.incrementAndGet();
			LOGGER.debug("Created new browser instance. New pool size: {}", getCurrentPoolSize());
			return browser;
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to create new browser instance", e);
			throw new RuntimeException("Failed to create browser", e);
		}
	}
	
	/**
	 * Closes a browser instance safely.
	 * 
	 * @param browser The browser to close
	 */
	@SuppressWarnings("checkstyle:IllegalCatch")
	private void closeBrowser(IBrowser browser)
	{
		try
		{
			browser.exit();
			LOGGER.debug("Closed browser instance");
		}
		catch (Exception e)
		{
			LOGGER.warn("Error closing browser instance", e);
		}
	}
	
	/**
	 * Gets the current total number of browsers in the pool (available + in use).
	 * 
	 * @return The current pool size
	 */
	private int getCurrentPoolSize()
	{
		return (int) this.totalBrowsersCreated.get();
	}
	
	/**
	 * Updates acquisition statistics.
	 * 
	 * @param acquisitionTimeNanos The acquisition time in nanoseconds
	 */
	private void updateAcquisitionStatistics(long acquisitionTimeNanos)
	{
		this.totalAcquisitions.incrementAndGet();
		this.totalAcquisitionTimeNanos.addAndGet(acquisitionTimeNanos);
		this.lastAcquisitionTime.set(Instant.now());
		
		// Update max acquisition time
		long currentMax = this.maxAcquisitionTimeNanos.get();
		while (acquisitionTimeNanos > currentMax)
		{
			if (this.maxAcquisitionTimeNanos.compareAndSet(currentMax, acquisitionTimeNanos))
			{
				break;
			}
			currentMax = this.maxAcquisitionTimeNanos.get();
		}
	}
} 

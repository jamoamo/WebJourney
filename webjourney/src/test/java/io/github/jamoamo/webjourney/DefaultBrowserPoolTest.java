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
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.PoolStatistics;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for DefaultBrowserPool.
 * 
 * @author James Amoore
 */
public class DefaultBrowserPoolTest
{
	@Mock
	private IBrowserFactory browserFactory;
	
	@Mock
	private IBrowserOptions browserOptions;
	
	@Mock
	private IBrowser mockBrowser;
	
	@Mock
	private IBrowserWindow mockWindow;
	
	private DefaultBrowserPool pool;
	private AutoCloseable mockitoCloseable;
	
	@BeforeEach
	public void setUp()
	{
		mockitoCloseable = MockitoAnnotations.openMocks(this);
		
		// Setup default mock behavior
		Mockito.when(browserOptions.isHeadless()).thenReturn(true);
		Mockito.when(browserOptions.acceptUnexpectedAlerts()).thenReturn(true);
		Mockito.when(mockBrowser.getActiveWindow()).thenReturn(mockWindow);
		Mockito.when(browserFactory.createBrowser(browserOptions)).thenReturn(mockBrowser);
		
		// Create pool with small size for testing
		pool = new DefaultBrowserPool(browserFactory, browserOptions, 1, 3);
	}
	
	@AfterEach
	public void tearDown() throws Exception
	{
		if (pool != null)
		{
			pool.shutdown();
		}
		if (mockitoCloseable != null)
		{
			mockitoCloseable.close();
		}
	}
	
	@Test
	public void testConstructorValidation()
	{
		// Test null browser factory
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultBrowserPool(null, browserOptions, 1, 3);
		});
		
		// Test null browser options
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultBrowserPool(browserFactory, null, 1, 3);
		});
		
		// Test negative min size
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultBrowserPool(browserFactory, browserOptions, -1, 3);
		});
		
		// Test max size less than min size
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			new DefaultBrowserPool(browserFactory, browserOptions, 5, 3);
		});
	}
	
	@Test
	public void testDefaultConstructor()
	{
		DefaultBrowserPool defaultPool = new DefaultBrowserPool(browserFactory, browserOptions);
		Assertions.assertFalse(defaultPool.isShutdown());
		defaultPool.shutdown();
	}
	
	@Test
	public void testAcquireBrowser() throws InterruptedException
	{
		IBrowser browser = pool.acquireBrowser();
		Assertions.assertNotNull(browser);
		Assertions.assertSame(mockBrowser, browser);
		
		// Verify statistics
		PoolStatistics stats = pool.getStatistics();
		Assertions.assertEquals(1, stats.getTotalAcquisitions());
		Assertions.assertEquals(0, stats.getFailedAcquisitions());
		Assertions.assertEquals(1, stats.getInUseBrowsers());
		Assertions.assertEquals(0, stats.getAvailableBrowsers());
	}
	
	@Test
	public void testReleaseBrowser() throws InterruptedException
	{
		// Acquire and release a browser
		IBrowser browser = pool.acquireBrowser();
		pool.releaseBrowser(browser);
		
		// Verify statistics
		PoolStatistics stats = pool.getStatistics();
		Assertions.assertEquals(1, stats.getTotalAcquisitions());
		Assertions.assertEquals(1, stats.getTotalReleases());
		Assertions.assertEquals(0, stats.getInUseBrowsers());
		Assertions.assertEquals(1, stats.getAvailableBrowsers());
	}
	
	@Test
	public void testReleaseNullBrowser()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			pool.releaseBrowser(null);
		});
	}
	
	@Test
	public void testAcquireBrowserWithTimeout() throws InterruptedException
	{
		// Acquire all available browsers
		IBrowser browser1 = pool.acquireBrowser();
		IBrowser browser2 = pool.acquireBrowser();
		IBrowser browser3 = pool.acquireBrowser();
		
		// Try to acquire with timeout - should timeout
		IBrowser browser4 = pool.acquireBrowser(100, TimeUnit.MILLISECONDS);
		Assertions.assertNull(browser4);
		
		// Release one browser and try again - should succeed
		pool.releaseBrowser(browser1);
		IBrowser browser5 = pool.acquireBrowser(100, TimeUnit.MILLISECONDS);
		Assertions.assertNotNull(browser5);
		
		// Cleanup
		pool.releaseBrowser(browser2);
		pool.releaseBrowser(browser3);
		pool.releaseBrowser(browser5);
	}
	
	@Test
	public void testAcquireBrowserWithInvalidTimeout()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			pool.acquireBrowser(-1, TimeUnit.SECONDS);
		});
		
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			pool.acquireBrowser(1, null);
		});
	}
	
	@Test
	public void testAcquireBrowserAsync() throws Exception
	{
		CompletableFuture<IBrowser> future = pool.acquireBrowserAsync();
		IBrowser browser = future.get(1, TimeUnit.SECONDS);
		
		Assertions.assertNotNull(browser);
		Assertions.assertSame(mockBrowser, browser);
		
		pool.releaseBrowser(browser);
	}
	
	@Test
	public void testPoolStatistics() throws InterruptedException
	{
		// Initial statistics
		PoolStatistics initialStats = pool.getStatistics();
		Assertions.assertEquals(1, initialStats.getMinSize());
		Assertions.assertEquals(3, initialStats.getMaxSize());
		Assertions.assertEquals(1, initialStats.getCurrentSize());
		Assertions.assertEquals(1, initialStats.getAvailableBrowsers());
		Assertions.assertEquals(0, initialStats.getInUseBrowsers());
		Assertions.assertFalse(initialStats.isShutdown());
		
		// Acquire a browser
		IBrowser browser = pool.acquireBrowser();
		PoolStatistics acquiredStats = pool.getStatistics();
		Assertions.assertEquals(1, acquiredStats.getTotalAcquisitions());
		Assertions.assertEquals(0, acquiredStats.getFailedAcquisitions());
		Assertions.assertEquals(1, acquiredStats.getInUseBrowsers());
		Assertions.assertEquals(0, acquiredStats.getAvailableBrowsers());
		Assertions.assertNotNull(acquiredStats.getLastAcquisitionTime());
		
		// Release the browser
		pool.releaseBrowser(browser);
		PoolStatistics releasedStats = pool.getStatistics();
		Assertions.assertEquals(1, releasedStats.getTotalReleases());
		Assertions.assertEquals(0, releasedStats.getInUseBrowsers());
		Assertions.assertEquals(1, releasedStats.getAvailableBrowsers());
		Assertions.assertNotNull(releasedStats.getLastReleaseTime());
	}
	
	@Test
	public void testShutdown() throws InterruptedException
	{
		Assertions.assertFalse(pool.isShutdown());
		
		pool.shutdown();
		Assertions.assertTrue(pool.isShutdown());
		
		// Should not be able to acquire after shutdown
		Assertions.assertThrows(IllegalStateException.class, () -> {
			try
			{
				pool.acquireBrowser();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
		});
		
		Assertions.assertThrows(IllegalStateException.class, () -> {
			pool.acquireBrowserAsync();
		});
	}
	
	@Test
	public void testReleaseAfterShutdown() throws InterruptedException
	{
		IBrowser browser = pool.acquireBrowser();
		pool.shutdown();
		pool.releaseBrowser(browser);
		
		// Browser should be closed when released after shutdown
		Mockito.verify(mockBrowser, Mockito.times(1)).exit();
	}
	
	@Test
	@Timeout(value = 5, unit = TimeUnit.SECONDS)
	public void testConcurrentAcquisition() throws Exception
	{
		// Reset mock to only count browser creations for this test
		Mockito.reset(browserFactory);
		Mockito.when(browserFactory.createBrowser(browserOptions)).thenReturn(mockBrowser);
		
		// Create a smaller pool to force contention
		DefaultBrowserPool smallPool = new DefaultBrowserPool(browserFactory, browserOptions, 1, 2);
		
		try
		{
			int numThreads = 4;
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch endLatch = new CountDownLatch(numThreads);
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);
			AtomicInteger successCount = new AtomicInteger(0);
			AtomicInteger failureCount = new AtomicInteger(0);
			
			// Create multiple threads trying to acquire browsers
			for (int i = 0; i < numThreads; i++)
			{
				executor.submit(() -> {
					try
					{
						startLatch.await();
						IBrowser browser = smallPool.acquireBrowser(200, TimeUnit.MILLISECONDS);
						if (browser != null)
						{
							successCount.incrementAndGet();
							Thread.sleep(400); // Hold browser longer than timeout to force contention
							smallPool.releaseBrowser(browser);
						}
						else
						{
							failureCount.incrementAndGet();
						}
					}
					catch (Exception e)
					{
						failureCount.incrementAndGet();
					}
					finally
					{
						endLatch.countDown();
					}
				});
			}
			
			startLatch.countDown();
			endLatch.await();
			executor.shutdown();
			
			// Verify that some acquisitions succeeded and some failed (due to pool size limit)
			Assertions.assertTrue(successCount.get() > 0, "Some acquisitions should succeed");
			Assertions.assertTrue(failureCount.get() > 0, "Some acquisitions should fail due to pool size limit");
			
			// Verify that no more than maxSize browsers were created
			Mockito.verify(browserFactory, Mockito.atMost(2)).createBrowser(browserOptions);
			
			// Verify final pool state
			PoolStatistics stats = smallPool.getStatistics();
			Assertions.assertEquals(2, stats.getAvailableBrowsers()); // Max size = 2, all browsers returned
			Assertions.assertEquals(0, stats.getInUseBrowsers());
		}
		finally
		{
			smallPool.shutdown();
		}
	}
	
	@Test
	public void testPoolSizeLimits() throws InterruptedException
	{
		// Create a pool with min=2, max=3
		DefaultBrowserPool limitedPool = new DefaultBrowserPool(browserFactory, browserOptions, 2, 3);
		
		try
		{
			// Should be able to acquire up to max size
			IBrowser browser1 = limitedPool.acquireBrowser();
			IBrowser browser2 = limitedPool.acquireBrowser();
			IBrowser browser3 = limitedPool.acquireBrowser();
			
			Assertions.assertNotNull(browser1);
			Assertions.assertNotNull(browser2);
			Assertions.assertNotNull(browser3);
			
			// Fourth acquisition should timeout
			IBrowser browser4 = limitedPool.acquireBrowser(100, TimeUnit.MILLISECONDS);
			Assertions.assertNull(browser4);
			
			// Release one and should be able to acquire again
			limitedPool.releaseBrowser(browser1);
			IBrowser browser5 = limitedPool.acquireBrowser(100, TimeUnit.MILLISECONDS);
			Assertions.assertNotNull(browser5);
			
			// Cleanup
			limitedPool.releaseBrowser(browser2);
			limitedPool.releaseBrowser(browser3);
			limitedPool.releaseBrowser(browser5);
		}
		finally
		{
			limitedPool.shutdown();
		}
	}
	
	@Test
	public void testUtilizationAndAvailabilityRates() throws InterruptedException
	{
		PoolStatistics stats = pool.getStatistics();
		
		// Initially: 1 available, 0 in use, max=3
		Assertions.assertEquals(0.0, stats.getUtilizationRate(), 0.001);
		Assertions.assertEquals(1.0 / 3.0, stats.getAvailabilityRate(), 0.001);
		
		// Acquire a browser
		IBrowser browser = pool.acquireBrowser();
		stats = pool.getStatistics();
		Assertions.assertEquals(1.0 / 3.0, stats.getUtilizationRate(), 0.001);
		Assertions.assertEquals(0.0, stats.getAvailabilityRate(), 0.001);
		
		// Release the browser
		pool.releaseBrowser(browser);
		stats = pool.getStatistics();
		Assertions.assertEquals(0.0, stats.getUtilizationRate(), 0.001);
		Assertions.assertEquals(1.0 / 3.0, stats.getAvailabilityRate(), 0.001);
	}
	
	@Test
	public void testAcquisitionSuccessRate() throws InterruptedException
	{
		// Initially no acquisitions
		PoolStatistics stats = pool.getStatistics();
		Assertions.assertEquals(1.0, stats.getAcquisitionSuccessRate(), 0.001);
		
		// Successful acquisition
		IBrowser browser = pool.acquireBrowser();
		stats = pool.getStatistics();
		Assertions.assertEquals(1.0, stats.getAcquisitionSuccessRate(), 0.001);
		
		// Try to acquire more than pool size (should fail)
		IBrowser browser2 = pool.acquireBrowser();
		IBrowser browser3 = pool.acquireBrowser();
		IBrowser browser4 = pool.acquireBrowser(100, TimeUnit.MILLISECONDS); // Should timeout
		
		stats = pool.getStatistics();
		Assertions.assertEquals(3.0 / 4.0, stats.getAcquisitionSuccessRate(), 0.001);
		
		// Cleanup
		pool.releaseBrowser(browser);
		pool.releaseBrowser(browser2);
		pool.releaseBrowser(browser3);
	}
} 
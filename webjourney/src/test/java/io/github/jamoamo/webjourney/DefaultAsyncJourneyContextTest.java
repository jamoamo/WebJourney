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
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserFactory;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.web.IBrowserPool;
import io.github.jamoamo.webjourney.api.web.PoolStatistics;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DefaultAsyncJourneyContext.
 * 
 * @author James Amoore
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultAsyncJourneyContextTest
{
	@Mock
	private IBrowserPool browserPool;
	
	@Mock
	private IBrowser browser;
	
	@Mock
	private IBrowserFactory browserFactory;
	
	@Mock
	private IBrowserOptions browserOptions;
	
	@Mock
	private IJourneyBreadcrumb breadcrumb;
	
	@Mock
	private IJourneyObserver observer;
	
	private ExecutorService executorService;
	private DefaultAsyncJourneyContext context;
	
	@BeforeEach
	void setUp()
	{
		executorService = Executors.newFixedThreadPool(2);
		context = new DefaultAsyncJourneyContext(browserPool, executorService);
	}
	
	@AfterEach
	void tearDown()
	{
		context.shutdown();
		executorService.shutdown();
	}
	
	@Test
	void testConstructorWithNullBrowserPool()
	{
		assertThrows(IllegalArgumentException.class, () ->
		{
			new DefaultAsyncJourneyContext(null, executorService);
		});
	}
	
	@Test
	void testConstructorWithNullExecutor()
	{
		assertThrows(IllegalArgumentException.class, () ->
		{
			new DefaultAsyncJourneyContext(browserPool, null);
		});
	}
	
	@Test
	void testGetBrowserPool()
	{
		assertSame(browserPool, context.getBrowserPool());
	}
	
	@Test
	void testGetActionExecutor()
	{
		assertSame(executorService, context.getActionExecutor());
	}
	
	@Test
	void testSetAndGetBrowser()
	{
		context.setBrowser(browser);
		assertSame(browser, context.getBrowser());
	}
	
	@Test
	void testSetBrowserWithNull()
	{
		assertThrows(IllegalArgumentException.class, () ->
		{
			context.setBrowser(null);
		});
	}
	
	@Test
	void testSetAndGetJourneyInput()
	{
		context.setJourneyInput("testKey", "testValue");
		assertEquals("testValue", context.getJourneyInput("testKey"));
	}
	
	@Test
	void testGetJourneyInputWithNonExistentKey()
	{
		assertNull(context.getJourneyInput("nonExistentKey"));
	}
	
	@Test
	void testSetAndGetJourneyObservers()
	{
		List<IJourneyObserver> observers = List.of(observer);
		context.setJourneyObservers(observers);
		
		List<IJourneyObserver> result = context.getJourneyObservers();
		assertEquals(1, result.size());
		assertSame(observer, result.get(0));
	}
	
	@Test
	void testSetJourneyBreadcrumb()
	{
		context.setJourneyBreadcrumb(breadcrumb);
		assertSame(breadcrumb, context.getJourneyBreadcrumb());
	}
	
	@Test
	void testAcquireBrowser()
	{
		CompletableFuture<IBrowser> browserFuture = CompletableFuture.completedFuture(browser);
		when(browserPool.acquireBrowserAsync()).thenReturn(browserFuture);
		
		CompletableFuture<IBrowser> result = context.acquireBrowser();
		
		assertNotNull(result);
		verify(browserPool).acquireBrowserAsync();
	}
	
	@Test
	void testAcquireBrowserWhenShutdown()
	{
		context.shutdown();
		
		assertThrows(IllegalStateException.class, () ->
		{
			context.acquireBrowser();
		});
	}
	
	@Test
	void testReleaseBrowser()
	{
		context.releaseBrowser(browser);
		verify(browserPool).releaseBrowser(browser);
	}
	
	@Test
	void testReleaseBrowserWithNull()
	{
		assertThrows(IllegalArgumentException.class, () ->
		{
			context.releaseBrowser(null);
		});
	}
	
	@Test
	void testExecuteActionAsync()
	{
		// Create a simple test action
		TestAction testAction = new TestAction();
		
		CompletableFuture<ActionResult> future = context.executeActionAsync(testAction);
		
		assertNotNull(future);
		ActionResult result = future.join();
		assertEquals(ActionResult.SUCCESS, result);
	}
	
	@Test
	void testExecuteActionAsyncWithNullAction()
	{
		assertThrows(IllegalArgumentException.class, () ->
		{
			context.executeActionAsync(null);
		});
	}
	
	@Test
	void testExecuteActionAsyncWhenShutdown()
	{
		context.shutdown();
		TestAction testAction = new TestAction();
		
		assertThrows(IllegalStateException.class, () ->
		{
			context.executeActionAsync(testAction);
		});
	}
	
	@Test
	void testIsShutdown()
	{
		assertFalse(context.isShutdown());
		context.shutdown();
		assertTrue(context.isShutdown());
	}
	
	/**
	 * Simple test action for testing async execution.
	 */
	private static class TestAction extends AWebAction
	{
		@Override
		protected ActionResult executeActionImpl(IJourneyContext context)
		{
			return ActionResult.SUCCESS;
		}
		
		@Override
		protected String getActionName()
		{
			return "TestAction";
		}
	}
} 
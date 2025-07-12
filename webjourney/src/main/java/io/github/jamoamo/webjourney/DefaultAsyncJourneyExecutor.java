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

import io.github.jamoamo.webjourney.api.IAsyncJourneyExecutor;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.web.DefaultBrowserOptions;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import io.github.jamoamo.webjourney.reserved.BreadcrumbPrinter;
import io.github.jamoamo.webjourney.reserved.JourneyBreadcrumb;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Default implementation of AsyncJourneyExecutor that provides asynchronous journey execution with CompletableFuture
 * support.
 * 
 * <p>
 * This implementation uses an internal ExecutorService to manage asynchronous execution of web journeys. It provides
 * proper exception handling, timeout support, and lifecycle management.
 * </p>
 * 
 * <h2>Thread Safety:</h2>
 * <p>
 * This class is thread-safe and can be used from multiple threads concurrently.
 * </p>
 * 
 * <h2>Resource Management:</h2>
 * <p>
 * Each journey execution manages its own browser instance lifecycle, ensuring proper cleanup even in case of exceptions
 * or timeouts.
 * </p>
 * 
 * <h2>Example Usage:</h2>
 * 
 * <pre>{@code
 * AsyncJourneyExecutor executor = new DefaultAsyncJourneyExecutor();
 * 
 * IJourney journey = JourneyBuilder.path().navigateTo("https://example.com").clickButton("#submit").build();
 * 
 * TravelOptions options = TravelOptions.builder().withBrowser(BrowserType.CHROME).build();
 * 
 * CompletableFuture<Void> future = executor.executeJourneyAsync(journey, options);
 * future.thenRun(() -> System.out.println("Journey completed successfully"));
 * 
 * // Don't forget to shutdown when done
 * executor.shutdown();
 * }</pre>
 * 
 * @author James Amoore
 * @see IAsyncJourneyExecutor
 * @see WebTraveller
 * @since 1.0.0
 */
public final class DefaultAsyncJourneyExecutor implements IAsyncJourneyExecutor
{
	private static final String MESSAGE_JOURNEY_TIMED_OUT = "Journey execution timed out after %d %s";
	
	private static final String LOGGER_CONTEXT_JOURNEY_LABEL = "label.AsyncJourney.id";
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAsyncJourneyExecutor.class);

	private final ExecutorService executorService;
	private final AtomicBoolean isShutdown = new AtomicBoolean(false);

	/**
	 * Creates a new DefaultAsyncJourneyExecutor with a default thread pool.
	 * 
	 * <p>
	 * The default thread pool is a cached thread pool that creates new threads as needed and reuses previously
	 * constructed threads when they are available.
	 * </p>
	 * 
	 * @since 1.1.0
	 */
	public DefaultAsyncJourneyExecutor()
	{
		this.executorService = Executors.newCachedThreadPool(r ->
		{
			Thread t = new Thread(r, "AsyncJourneyExecutor-" + System.currentTimeMillis());
			t.setDaemon(true);
			return t;
		});
	}

	/**
	 * Creates a new DefaultAsyncJourneyExecutor with the specified ExecutorService.
	 * 
	 * @param executorService The ExecutorService to use for asynchronous execution
	 * @throws IllegalArgumentException if executorService is null
	 * @since 1.1.0
	 */
	public DefaultAsyncJourneyExecutor(ExecutorService executorService)
	{
		if (executorService == null)
		{
			throw new IllegalArgumentException("ExecutorService cannot be null");
		}
		this.executorService = executorService;
	}

	@Override
	public CompletableFuture<Void> executeJourneyAsync(IJourney journey, ITravelOptions travelOptions)
	{
		validateInputs(journey, travelOptions);

		if (this.isShutdown.get())
		{
			CompletableFuture<Void> future = new CompletableFuture<>();
			future.completeExceptionally(new IllegalStateException("AsyncJourneyExecutor has been shut down"));
			return future;
		}

		return CompletableFuture.runAsync(() ->
		{
			executeJourneySync(journey, travelOptions);
		}, this.executorService);
	}

	@Override
	@SuppressWarnings("checkstyle:MethodLength")
	public CompletableFuture<Void> executeJourneyAsync(IJourney journey, ITravelOptions travelOptions, long timeout,
			TimeUnit unit)
	{
		validateInputs(journey, travelOptions, timeout, unit);

		CompletableFuture<Void> future = executeJourneyAsync(journey, travelOptions);

		// Apply timeout to the future
		return future.orTimeout(timeout, unit).exceptionally(throwable ->
		{
			if (throwable instanceof TimeoutException)
			{
				LOGGER.warn(MESSAGE_JOURNEY_TIMED_OUT, timeout, unit);
				throw new JourneyTimedOutException(timeout, unit, throwable);
			}
			else if (throwable.getCause() instanceof TimeoutException)
			{
				LOGGER.warn(MESSAGE_JOURNEY_TIMED_OUT, timeout, unit);
				throw new JourneyTimedOutException(timeout, unit, throwable.getCause());
			}
			else
			{
				// Re-throw other exceptions
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException) throwable;
				}
				else
				{
					throw new JourneyException("Journey execution failed", throwable);
				}
			}
		});
	}

	@Override
	public void shutdown()
	{
		if (this.isShutdown.compareAndSet(false, true))
		{
			LOGGER.info("Shutting down AsyncJourneyExecutor");
			this.executorService.shutdown();
		}
	}

	@Override
	public void shutdownNow()
	{
		if (this.isShutdown.compareAndSet(false, true))
		{
			LOGGER.info("Forcibly shutting down AsyncJourneyExecutor");
			this.executorService.shutdownNow();
		}
	}

	@Override
	public boolean isShutdown()
	{
		return this.isShutdown.get();
	}

	@Override
	public boolean isTerminated()
	{
		return this.executorService.isTerminated();
	}

	/**
	 * Validates the input parameters for journey execution.
	 * 
	 * @param journey       The journey to validate
	 * @param travelOptions The travel options to validate
	 * @throws IllegalArgumentException if any parameter is null
	 */
	private void validateInputs(IJourney journey, ITravelOptions travelOptions)
	{
		if (journey == null)
		{
			throw new IllegalArgumentException("Journey cannot be null");
		}
		if (travelOptions == null)
		{
			throw new IllegalArgumentException("TravelOptions cannot be null");
		}
	}

	/**
	 * Validates the input parameters for journey execution.
	 * 
	 * @param journey       The journey to validate
	 * @param travelOptions The travel options to validate
	 * @param timeout       The timeout to validate
	 * @param unit          The time unit to validate
	 * @throws IllegalArgumentException if any parameter is null
	 */
	private void validateInputs(IJourney journey, ITravelOptions travelOptions, long timeout, TimeUnit unit)
	{
		validateInputs(journey, travelOptions);

		if (timeout < 0)
		{
			throw new IllegalArgumentException("Timeout cannot be negative");
		}

		if (unit == null)
		{
			throw new IllegalArgumentException("TimeUnit cannot be null");
		}
	}

	/**
	 * Executes a journey synchronously within the async context.
	 * 
	 * <p>
	 * This method mirrors the logic from WebTraveller but is designed to be called within an async execution context. It
	 * handles browser lifecycle, logging, and exception handling.
	 * </p>
	 * 
	 * @param journey       The journey to execute
	 * @param travelOptions The travel options for execution
	 * @throws JourneyException if the journey execution fails
	 */
	@SuppressWarnings("checkstyle:MethodLength")
	private void executeJourneySync(IJourney journey, ITravelOptions travelOptions)
	{
		String journeyId = UUID.randomUUID().toString();
		MDC.put(LOGGER_CONTEXT_JOURNEY_LABEL, journeyId);

		try
		{
			LOGGER.info("Starting async journey execution: {}", journeyId);

			IPreferredBrowserStrategy browserStrategy = travelOptions.getPreferredBrowserStrategy();
			IBrowser browser = browserStrategy.getPreferredBrowser(new DefaultBrowserOptions());
			JourneyContext context = new JourneyContext();
			IJourneyBreadcrumb breadcrumb = new JourneyBreadcrumb();

			context.setJourneyBreadcrumb(breadcrumb);
			context.setBrowser(browser);
			context.setJourneyObservers(travelOptions.getJourneyObservers());

			try
			{
				journey.doJourney(context);
				LOGGER.info("Async journey completed successfully: {}", journeyId);
			}
			catch (JourneyException ex)
			{
				String breadcrumbString = getBreadcrumb(ex);
				LOGGER.error("Async journey failed ({}): {}", breadcrumbString, ex.getMessage());
				throw ex;
			}
			finally
			{
				closeBrowserSafely(browser, journeyId);
			}
		}
		finally
		{
			MDC.remove(LOGGER_CONTEXT_JOURNEY_LABEL);
		}
	}

	/**
	 * Safely closes a browser instance, handling any exceptions that may occur.
	 * 
	 * @param browser   The browser to close
	 * @param journeyId The journey ID for logging context
	 */
	@SuppressWarnings("checkstyle:IllegalCatch")
	private void closeBrowserSafely(IBrowser browser, String journeyId)
	{
		try
		{
			browser.exit();
		}
		catch (Exception e)
		{
			LOGGER.warn("Error closing browser for journey {}: {}", journeyId, e.getMessage());
		}
	}

	/**
	 * Extracts breadcrumb information from a JourneyException for logging.
	 * 
	 * @param ex The JourneyException to extract breadcrumb from
	 * @return A string representation of the breadcrumb trail
	 */
	private String getBreadcrumb(JourneyException ex)
	{
		String breadcrumbString = "<unknown context>";
		if (ex.getBreadcrumb() != null)
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try
			{
				new BreadcrumbPrinter().printBreadCrumb(stream, ex.getBreadcrumb());
				breadcrumbString = stream.toString();
			}
			catch (IOException e)
			{
				LOGGER.debug("Failed to print breadcrumb: {}", e.getMessage());
			}
		}
		return breadcrumbString;
	}
}

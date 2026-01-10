/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XEnvironmentalNavigationError;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
final class SeleniumWindow implements IBrowserWindow
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWindow.class);
	private final String windowName;
	private boolean active;
	private final RemoteWebDriver webDriver;
	private IWebPage currentPage;
	
	private boolean screenshotEnabled = false;
	private NavigationRetryConfig retryConfig = new NavigationRetryConfig();
	
	SeleniumWindow(String windowName, RemoteWebDriver webDriver)
	{
		this.windowName = windowName;
		this.webDriver = webDriver;
		this.currentPage = new SeleniumPage(this.webDriver);
		this.active = false;
	}
	
	/**
	 * Sets the retry configuration for navigation operations.
	 * @param config the retry configuration
	 */
	void setRetryConfig(NavigationRetryConfig config)
	{
		this.retryConfig = config != null ? config : new NavigationRetryConfig();
	}
	
	private void checkWindowIsActive()
	{
		if(!this.active)
		{
			throw new XInactiveWindowException();
		}
	}

	@Override
	public String getCurrentUrl()
	{
		checkWindowIsActive();
		return this.webDriver.getCurrentUrl();
	}

	@Override
	public IWebPage getCurrentPage()
	{
		checkWindowIsActive();
		return this.currentPage;
	}

	@Override
	public IWebPage refreshCurrentPage()
		throws XNavigationError
	{
		checkWindowIsActive();
		
		executeWithRetry(
			() -> this.webDriver.navigate().refresh(),
			"refreshCurrentPage()"
		);
		
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public void close()
	{
		checkWindowIsActive();
		this.webDriver.close();
		this.currentPage = new SeleniumPage(this.webDriver);
	}

	/**
	 * Checks if a WebDriverException is an environmental error that should be retried.
	 * @param ex the exception to check
	 * @return true if the error is environmental/retryable, false otherwise
	 */
	private boolean isEnvironmentalError(WebDriverException ex)
	{
		String message = ex.getMessage();
		if (message == null)
		{
			return false;
		}
		
		// Check for common connection/network errors
		return message.contains("ERR_CONNECTION_REFUSED") ||
			   message.contains("ERR_CONNECTION_RESET") ||
			   message.contains("ERR_CONNECTION_CLOSED") ||
			   message.contains("ERR_CONNECTION_TIMED_OUT") ||
			   message.contains("ERR_NETWORK_CHANGED") ||
			   message.contains("ERR_INTERNET_DISCONNECTED") ||
			   message.contains("ERR_TIMED_OUT") ||
			   message.contains("ERR_NAME_NOT_RESOLVED") ||
			   message.contains("ERR_PROXY_CONNECTION_FAILED") ||
			   message.contains("net::ERR_") && (
				   message.contains("CONNECTION") || 
				   message.contains("TIMEOUT") || 
				   message.contains("NETWORK")
			   );
	}
	
	/**
	 * Executes a navigation operation with retry logic.
	 * @param operation the navigation operation to execute
	 * @param operationDescription description of the operation for logging
	 * @throws XNavigationError if navigation fails after all retries
	 */
	private void executeWithRetry(NavigationOperation operation, String operationDescription)
		throws XNavigationError
	{
		int attempts = 0;
		WebDriverException lastException = null;
		
		while (attempts < this.retryConfig.getMaxAttempts())
		{
			attempts++;
			try
			{
				operation.execute();
				if (attempts > 1)
				{
					LOGGER.info(String.format("Window [%s] %s succeeded on attempt %d", 
						this.windowName, operationDescription, attempts));
				}
				return; // Success
			}
			catch (WebDriverException ex)
			{
				lastException = ex;
				
				if (isEnvironmentalError(ex))
				{
					if (attempts < this.retryConfig.getMaxAttempts())
					{
						String warnMsg = String.format(
							"Window [%s] %s failed with environmental error on attempt %d: %s. Retrying...", 
							this.windowName, operationDescription, attempts, ex.getMessage());
						LOGGER.warn(warnMsg);
						
						// Wait before retrying
						if (this.retryConfig.getRetryDelayMillis() > 0)
						{
							try
							{
								Thread.sleep(this.retryConfig.getRetryDelayMillis());
							}
							catch (InterruptedException ie)
							{
								Thread.currentThread().interrupt();
								throw new XNavigationError("Navigation interrupted during retry delay");
							}
						}
					}
					else
					{
						// Max attempts reached with environmental error
						String errorMsg = String.format(
							"Window [%s] %s failed after %d attempts with environmental error", 
							this.windowName, operationDescription, attempts);
						LOGGER.error(errorMsg);
						throw new XEnvironmentalNavigationError(
							String.format("%s: %s", operationDescription, ex.getMessage()), 
							attempts, 
							ex
						);
					}
				}
				else
				{
					// Non-environmental error, don't retry
					LOGGER.error(String.format("Window [%s] %s failed with non-retryable error: %s", 
						this.windowName, operationDescription, ex.getMessage()));
					throw new XNavigationError(String.format("%s: %s", operationDescription, ex.getMessage()));
				}
			}
		}
		
		// Should not reach here, but just in case
		if (lastException != null)
		{
			throw new XEnvironmentalNavigationError(
				String.format("%s: %s", operationDescription, lastException.getMessage()), 
				attempts, 
				lastException
			);
		}
	}
	
	/**
	 * Functional interface for navigation operations that can be retried.
	 */
	@FunctionalInterface
	private interface NavigationOperation
	{
		void execute() throws WebDriverException;
	}

	@Override
	public IWebPage navigateToUrl(URL url)
		throws XNavigationError
	{
		checkWindowIsActive();
		LOGGER.info(String.format("Window [%s] navigating to url %s", this.windowName, url.toString()));
		
		executeWithRetry(
			() -> this.webDriver.navigate().to(url),
			"navigateToUrl(" + url.toString() + ")"
		);
		
		takeScreenshot();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateBack()
		throws XNavigationError
	{
		checkWindowIsActive();
		LOGGER.info(String.format("Window [%s] navigating back", this.windowName));
		
		executeWithRetry(
			() -> this.webDriver.navigate().back(),
			"navigateBack()"
		);
		
		takeScreenshot();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}

	@Override
	public IWebPage navigateForward()
		throws XNavigationError
	{
		checkWindowIsActive();
		LOGGER.info(String.format("Window [%s] navigating forward", this.windowName));
		
		executeWithRetry(
			() -> this.webDriver.navigate().forward(),
			"navigateForward()"
		);
		
		takeScreenshot();
		this.currentPage = new SeleniumPage(this.webDriver);
		return this.currentPage;
	}
	
	void setActive(boolean active)
	{
		this.active = active;
	}
	
	boolean isActive()
	{
		return this.active;
	}

	@Override
	public String getName()
	{
		return this.windowName;
	}
	
	private void takeScreenshot()
	{
		if(!this.screenshotEnabled)
		{
			return;
		}
		
		try
		{
			LOGGER.debug("Taking Screenshot");
			File srcFile = ((TakesScreenshot)this.webDriver).getScreenshotAs(OutputType.FILE);
			
			if(srcFile == null)
			{
				return;
			}
			
			LocalDateTime time = LocalDateTime.now();
			var timeStr = time.format(DateTimeFormatter.ofPattern("YYYYMMddHHmmss"));
			String fileName = String.format("output/screenshot/screenshot-%s.png", timeStr);
			File destFile = new File(fileName);
			LOGGER.debug(String.format("Copying Screenshot [%s]", fileName));
			FileUtils.copyFile(srcFile, destFile);
		}
		catch(IOException ioe)
		{
			LOGGER.error("Could not take screenshot.", ioe);
		}
	}

	@Override
	public String getTitle()
	{
		return this.webDriver.getTitle();
	}
}

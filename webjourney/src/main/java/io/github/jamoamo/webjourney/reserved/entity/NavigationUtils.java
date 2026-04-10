package io.github.jamoamo.webjourney.reserved.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for robust browser navigation operations.
 * 
 * @author James Amoore
 */
final class NavigationUtils
{
	private static final Logger logger = LoggerFactory.getLogger(NavigationUtils.class);

	private NavigationUtils()
	{
	}

	/**
	 * Navigates back, retrying on failure up to the specified maximum attempts.
	 * 
	 * @param reader        the value reader handling the browser interaction
	 * @param maxAttempts   the maximum number of attempts
	 * @param backoffMillis the time to wait between attempts
	 * @throws IllegalStateException if all attempts fail or the thread is interrupted
	 */
	static void retryNavigateBack(IValueReader reader, int maxAttempts, long backoffMillis)
	{
		int attempts = 0;
		while(true)
		{
			try
			{
				reader.navigateBack();
				break;
			}
			catch(Exception e)
			{
				attempts++;
				if(attempts >= maxAttempts)
				{
					logger.error("Failed to navigate back after creating entity.", e);
					throw new IllegalStateException("Failed to navigate back after creating entity", e);
				}
				logger.warn("Failed to navigate back, retrying...", e);
				try
				{
					Thread.sleep(backoffMillis);
				}
				catch(InterruptedException ie)
				{
					Thread.currentThread().interrupt();
					throw new IllegalStateException("Navigation back interrupted", ie);
				}
			}
		}
	}
}

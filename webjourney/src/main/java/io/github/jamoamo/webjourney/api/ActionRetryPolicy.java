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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Policy for retrying actions.
 * @author James Amoore
 */
public class ActionRetryPolicy
{
	private final int maxRetries;
	private final long retryDelay;
	private final List<Class<? extends Throwable>> retryableExceptions;

	/**
	 * Creates a new retry policy.
	 * @param maxRetries the maximum number of retries.
	 * @param retryDelay the delay between retries in milliseconds.
	 * @param retryableExceptions the exceptions that should trigger a retry.
	 */
	public ActionRetryPolicy(int maxRetries, long retryDelay, List<Class<? extends Throwable>> retryableExceptions)
	{
		this.maxRetries = maxRetries;
		this.retryDelay = retryDelay;
		this.retryableExceptions = retryableExceptions != null ? new ArrayList<>(retryableExceptions) : new ArrayList<>();
	}

	/**
	 * Creates a new retry policy with no retries.
	 */
	public ActionRetryPolicy()
	{
		this(0, 0, Collections.emptyList());
	}

	/**
	 * @return the maximum number of retries.
	 */
	public int getMaxRetries()
	{
		return maxRetries;
	}

	/**
	 * @return the delay between retries in milliseconds.
	 */
	public long getRetryDelay()
	{
		return retryDelay;
	}

	/**
	 * @return the exceptions that should trigger a retry.
	 */
	public List<Class<? extends Throwable>> getRetryableExceptions()
	{
		return Collections.unmodifiableList(retryableExceptions);
	}

	/**
	 * Checks if a retry should be attempted.
	 * @param attempt the current attempt number (1-based).
	 * @param ex the exception that occurred.
	 * @return true if a retry should be attempted.
	 */
	public boolean shouldRetry(int attempt, Throwable ex)
	{
		if(attempt > maxRetries)
		{
			return false;
		}

		if(retryableExceptions.isEmpty())
		{
			// If no exceptions specified, retry on all Exceptions (but not Errors)
			return ex instanceof Exception;
		}

		for(Class<? extends Throwable> cls : retryableExceptions)
		{
			if(cls.isInstance(ex))
			{
				return true;
			}
		}

		return false;
	}
}

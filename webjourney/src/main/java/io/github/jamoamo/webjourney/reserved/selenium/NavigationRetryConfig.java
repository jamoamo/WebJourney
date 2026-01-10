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

/**
 * Configuration for navigation retry behavior.
 * 
 * @author James Amoore
 */
public class NavigationRetryConfig
{
	private final int maxAttempts;
	private final long retryDelayMillis;
	
	/**
	 * Creates a default configuration with 3 max attempts and 1 second delay.
	 */
	public NavigationRetryConfig()
	{
		this(3, 1000);
	}
	
	/**
	 * Creates a configuration with the specified max attempts and delay.
	 * @param maxAttempts the maximum number of attempts (must be at least 1)
	 * @param retryDelayMillis the delay between retries in milliseconds
	 */
	public NavigationRetryConfig(int maxAttempts, long retryDelayMillis)
	{
		if (maxAttempts < 1)
		{
			throw new IllegalArgumentException("maxAttempts must be at least 1");
		}
		if (retryDelayMillis < 0)
		{
			throw new IllegalArgumentException("retryDelayMillis must be non-negative");
		}
		this.maxAttempts = maxAttempts;
		this.retryDelayMillis = retryDelayMillis;
	}
	
	/**
	 * Gets the maximum number of attempts.
	 * @return the maximum number of attempts
	 */
	public int getMaxAttempts()
	{
		return this.maxAttempts;
	}
	
	/**
	 * Gets the delay between retries in milliseconds.
	 * @return the delay in milliseconds
	 */
	public long getRetryDelayMillis()
	{
		return this.retryDelayMillis;
	}
}


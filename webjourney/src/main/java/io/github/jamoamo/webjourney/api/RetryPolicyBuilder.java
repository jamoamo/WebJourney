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

import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import java.time.Duration;

/**
 * Builder for {@link IRetryPolicy}.
 * 
 * @author James Amoore
 */
public final class RetryPolicyBuilder
{
	private int maxRetries = 3;
	private Duration delay = Duration.ofSeconds(1);

	private RetryPolicyBuilder()
	{
	}

	/**
	 * Creates a new instance of the builder.
	 * 
	 * @return a new builder
	 */
	public static RetryPolicyBuilder builder()
	{
		return new RetryPolicyBuilder();
	}

	/**
	 * Set the maximum number of retries before giving up.
	 * 
	 * @param max the maximum number of retries
	 * @return the current builder
	 */
	public RetryPolicyBuilder maxRetries(int max)
	{
		if(max < 0)
		{
			throw new IllegalArgumentException("maxRetries cannot be negative");
		}
		this.maxRetries = max;
		return this;
	}

	/**
	 * Set the delay between retries.
	 * 
	 * @param retryDelay the delay duration
	 * @return the current builder
	 */
	public RetryPolicyBuilder delay(Duration retryDelay)
	{
		if(retryDelay == null)
		{
			throw new IllegalArgumentException("retryDelay cannot be null");
		}
		if(retryDelay.isNegative() || retryDelay.isZero())
		{
			throw new IllegalArgumentException("retryDelay must be strictly positive");
		}
		this.delay = retryDelay;
		return this;
	}

	/**
	 * Builds an {@link IRetryPolicy} from the configured settings.
	 * 
	 * @return a built IRetryPolicy
	 */
	public IRetryPolicy build()
	{
		final RetryPolicy<Object> policy = RetryPolicy.builder()
			.handle(Exception.class)
			.withDelay(this.delay)
			.withMaxRetries(this.maxRetries)
			.build();

		return new IRetryPolicy()
		{
			@Override
			public <T> T execute(CallableAction<T> action) throws Exception
			{
				try
				{
					return Failsafe.with(policy).get(action::call);
				}
				catch(FailsafeException e)
				{
					if(e.getCause() instanceof Exception exception)
					{
						throw exception;
					}
					throw e;
				}
			}
		};
	}
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder for {@link IRetryPolicy}.
 * <p>
 * By default a refused connection (see {@link ConnectionFailures#isConnectionRefused(Throwable)}) is never retried,
 * because retrying it will not fix it and only adds to whatever is causing the refusal. Use
 * {@link #abortOn(Predicate)} to add further failures that must not be retried, and {@link #clearAbortRules()} to
 * opt out of the default.
 *
 * @author James Amoore
 */
public final class RetryPolicyBuilder
{
	private static final Logger logger = LoggerFactory.getLogger(RetryPolicyBuilder.class);
	private int maxRetries = 3;
	private Duration delay = Duration.ofSeconds(1);
	private final List<Predicate<Throwable>> abortRules = new ArrayList<>(List.of(ConnectionFailures::isConnectionRefused));

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
	 * Adds a rule for failures that must not be retried. A failure that matches any rule is thrown straight away
	 * rather than retried. Can be called more than once, the rules are combined. This is in addition to the default
	 * rule (see the class description), unless {@link #clearAbortRules()} has removed it.
	 *
	 * @param rule matches the failures that must not be retried. It is given the failure thrown by the action, so
	 * check its causes too if they matter.
	 * @return the current builder
	 */
	public RetryPolicyBuilder abortOn(Predicate<Throwable> rule)
	{
		if(rule == null)
		{
			throw new IllegalArgumentException("rule cannot be null");
		}
		this.abortRules.add(rule);
		return this;
	}

	/**
	 * Removes every abort rule, including the default one, so that a refused connection is retried like any other
	 * failure. Rules added with {@link #abortOn(Predicate)} afterwards still apply.
	 *
	 * @return the current builder
	 */
	public RetryPolicyBuilder clearAbortRules()
	{
		this.abortRules.clear();
		return this;
	}

	/**
	 * Builds an {@link IRetryPolicy} from the configured settings. A failure matching an abort rule is not retried;
	 * by default that means a refused connection (see {@link ConnectionFailures#isConnectionRefused(Throwable)}).
	 *
	 * @return a built IRetryPolicy
	 */
	public IRetryPolicy build()
	{
		final List<Predicate<Throwable>> rules = List.copyOf(this.abortRules);
		final RetryPolicy<Object> policy = RetryPolicy.builder()
			.handle(Exception.class)
			.abortOn((Throwable failure) -> rules.stream().anyMatch(rule -> rule.test(failure)))
			.withDelay(this.delay)
			.withMaxRetries(this.maxRetries)
			.onRetry(e -> logger.info("Action failed, retrying... (Attempt #{})", e.getAttemptCount(), e.getLastException()))
			.onAbort(e -> logger.info("Not retrying, an abort rule matched (attempt #{}, {} ms elapsed): {}",
				e.getAttemptCount(), e.getElapsedTime().toMillis(), String.valueOf(e.getException())))
			.onRetriesExceeded(e -> logger.warn("Max retries exceeded (attempt #{}, {} ms elapsed)",
				e.getAttemptCount(), e.getElapsedTime().toMillis(), e.getException()))
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

/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import io.github.jamoamo.webjourney.api.IJourneyPassenger;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IJourneyBrowserArguments;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A journey context that behaves exactly like the one it wraps, except that it also counts the retries of the actions
 * run with it. Used to find out how many retries a best-effort sub journey needed. The wrapped context, and so the
 * observers the journey was configured with, is left unchanged.
 *
 * @author James Amoore
 */
final class RetryCountingContext implements IJourneyContext
{
	private final IJourneyContext delegate;
	private final AtomicInteger retryCount = new AtomicInteger();
	private final IJourneyObserver counter = new IJourneyObserver()
	{
		@Override
		public void actionStarted(AWebAction action)
		{
		}

		@Override
		public void actionEnded(AWebAction action)
		{
		}

		@Override
		public void actionRetried(AWebAction action, int attempt, Throwable failure)
		{
			RetryCountingContext.this.retryCount.incrementAndGet();
		}

		@Override
		public void entityCreated(Object object)
		{
		}

		@Override
		public void entityCreationStarted(Class<?> entityClass)
		{
		}
	};

	RetryCountingContext(IJourneyContext delegate)
	{
		this.delegate = delegate;
	}

	/**
	 * How many actions run with this context have been retried so far.
	 *
	 * @return the retry count
	 */
	int getRetryCount()
	{
		return this.retryCount.get();
	}

	@Override
	public IBrowser getBrowser()
	{
		return this.delegate.getBrowser();
	}

	@Override
	public void setJourneyInput(String inputType, Object inputValue)
	{
		this.delegate.setJourneyInput(inputType, inputValue);
	}

	@Override
	public Object getJourneyInput(String inputType)
	{
		return this.delegate.getJourneyInput(inputType);
	}

	@Override
	public List<IJourneyObserver> getJourneyObservers()
	{
		List<IJourneyObserver> observers = new ArrayList<>();
		List<IJourneyObserver> delegateObservers = this.delegate.getJourneyObservers();
		if(delegateObservers != null)
		{
			observers.addAll(delegateObservers);
		}
		observers.add(this.counter);
		return observers;
	}

	@Override
	public void setJourneyObservers(List<IJourneyObserver> observers)
	{
		this.delegate.setJourneyObservers(observers);
	}

	@Override
	public List<IJourneyPassenger> getJourneyPassengers()
	{
		return this.delegate.getJourneyPassengers();
	}

	@Override
	public void setJourneyPassengers(List<IJourneyPassenger> passengers)
	{
		this.delegate.setJourneyPassengers(passengers);
	}

	@Override
	public IJourneyBreadcrumb getJourneyBreadcrumb()
	{
		return this.delegate.getJourneyBreadcrumb();
	}

	@Override
	public IJourneyBrowserArguments getBrowserArguments()
	{
		return this.delegate.getBrowserArguments();
	}

	@Override
	public ITravelOptions getOptions()
	{
		return this.delegate.getOptions();
	}
}

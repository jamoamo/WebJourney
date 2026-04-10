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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.IJourneyObserver;
import io.github.jamoamo.webjourney.api.web.DefaultJourneyBrowserArguments;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IJourneyBrowserArguments;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Journey context implementation that maintains state for a single journey.
 * This implementation is thread-safe and can be safely accessed from multiple threads.
 *
 * @author James Amoore
 */
public class JourneyContext implements IJourneyContext
{
	private IBrowser browser;
	private IJourneyBreadcrumb breadcrumb;
	private final Map<String, Object> inputs = new ConcurrentHashMap<>(2);
	private final List<IJourneyObserver> journeyObservers = new CopyOnWriteArrayList<>();
	private final IJourneyBrowserArguments browserArguments = new DefaultJourneyBrowserArguments();
	
	void setBrowser(IBrowser browser)
	{
		this.browser = browser;
	}
	
	@Override
	public IBrowser getBrowser()
	{
		return this.browser;
	}

	@Override
	public void setJourneyInput(String inputType, Object inputValue)
	{
		this.inputs.put(inputType, inputValue);
	}

	@Override
	public Object getJourneyInput(String inputType)
	{
		return this.inputs.get(inputType);
	}

	@Override
	public List<IJourneyObserver> getJourneyObservers()
	{
		return Collections.unmodifiableList(this.journeyObservers);
	}

	@Override
	public void setJourneyObservers(List<IJourneyObserver> observers)
	{
		this.journeyObservers.addAll(observers);
	}

	@Override
	public IJourneyBreadcrumb getJourneyBreadcrumb()
	{
		return this.breadcrumb;
	}
	
	public void setJourneyBreadcrumb(IJourneyBreadcrumb breadcrumb)
	{
		this.breadcrumb = breadcrumb;
	}

	@Override
	public IJourneyBrowserArguments getBrowserArguments()
	{
		return this.browserArguments;
	}
}

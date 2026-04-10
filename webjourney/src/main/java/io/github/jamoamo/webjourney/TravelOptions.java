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

import io.github.jamoamo.webjourney.api.IJourneyObserver;
import io.github.jamoamo.webjourney.api.ITravelOptions;
import io.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import io.github.jamoamo.webjourney.api.web.PreferredBrowserStrategy;
import io.github.jamoamo.webjourney.reserved.selenium.ChromeBrowserFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A set of options for travelling a web journey.
 * @author James Amoore
 */
public final class TravelOptions implements ITravelOptions
{
	private IPreferredBrowserStrategy preferredBrowserStrategy 
			  = new PreferredBrowserStrategy(new ChromeBrowserFactory());
	
	private List<IJourneyObserver> journeyObservers = new ArrayList<>();
	
	/**
	 * Sets the preferred browser strategy to use. 
	 * @param strategy the strategy to use to create the preferred browser.
	 */
	@Override
	public void setPreferredBrowserStrategy(IPreferredBrowserStrategy strategy)
	{
		this.preferredBrowserStrategy = strategy;
	}
	
	/**
	 * Returns the preferred browser strategy.
	 * @return the preferred browser strategy.
	 */
	@Override
	public IPreferredBrowserStrategy getPreferredBrowserStrategy()
	{
		return this.preferredBrowserStrategy;
	}

	/**
	 * Adds an observer to the journey.
	 * @param observer the observer to add.
	 */
	@Override
	public void addObserver(IJourneyObserver observer)
	{
		this.journeyObservers.add(observer);
	}
	
	/**
	 * Retrieves the journey observers.
	 * @return the journey observers
	 */
	public List<IJourneyObserver> getJourneyObservers()
	{
		return Collections.unmodifiableList(this.journeyObservers);
	}
}

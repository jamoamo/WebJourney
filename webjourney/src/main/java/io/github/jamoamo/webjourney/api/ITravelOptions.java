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

import io.github.jamoamo.webjourney.api.web.IPreferredBrowserStrategy;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public interface ITravelOptions
{
	/**
	 * Sets the preferred browser strategy to use.
	 * @param strategy the strategy to use to create the preferred browser.
	 */
	void setPreferredBrowserStrategy(IPreferredBrowserStrategy strategy);

	/**
	 * Returns the preferred browser strategy.
	 * @return the preferred browser strategy.
	 */
	IPreferredBrowserStrategy getPreferredBrowserStrategy();
	
	/**
	 * Adds an observer to the journey.
	 * @param observer the observer to add.
	 */
	void addObserver(IJourneyObserver observer);

	/**
	 * Retrieves the journey observers.
	 * @return the journey observers
	 */
	List<IJourneyObserver> getJourneyObservers();
	
}

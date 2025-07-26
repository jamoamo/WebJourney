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
package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.api.web.IBrowser;
import java.util.List;

/**
 * Represents the context of a specific browser journey, providing access to the browser instance,
 * journey-specific inputs, observers, and breadcrumb trail.
 * 
 * @author James Amoore
 * @see IBrowser
 * @see IJourneyObserver
 * @see IJourneyBreadcrumb
 * @since 1.0.0
 */
public interface IJourneyContext
{
	
	/**
	 * Returns the browser instance associated with the current journey.
	 * @return The {@link IBrowser} instance for interacting with the web page.
	 * @since 1.0.0
	 */
	IBrowser getBrowser();
	
	/**
	 * Sets an input value for the journey. This allows sharing data across different actions within the journey.
	 * 
	 * @param inputType The type identifier for the input.
	 * @param inputValue The value to be stored for this input type.
	 * @since 1.0.0
	 */
	void setJourneyInput(String inputType, Object inputValue);
	
	/**
	 * Retrieves an input value for the journey based on the input type.
	 * 
	 * @param inputType The type identifier for the input.
	 * @return The stored input value, or null if no value has been set for this type.
	 * @since 1.0.0
	 */
	Object getJourneyInput(String inputType);
	
	/**
	 * Returns all the observers registered for this journey. Observers can monitor journey execution.
	 * @return A list of {@link IJourneyObserver} instances.
	 * @since 1.0.0
	 */
	List<IJourneyObserver> getJourneyObservers();
	
	/**
	 * Sets the observers for this journey. These observers will monitor journey execution.
	 * @param observers The list of {@link IJourneyObserver} instances to set.
	 * @since 1.0.0
	 */
	void setJourneyObservers(List<IJourneyObserver> observers);
	
	/**
	 * Returns the breadcrumb trail for the current journey, providing a trace of executed actions.
	 * @return The {@link IJourneyBreadcrumb} instance tracking the journey's path.
	 * @since 1.0.0
	 */
	IJourneyBreadcrumb getJourneyBreadcrumb();
}

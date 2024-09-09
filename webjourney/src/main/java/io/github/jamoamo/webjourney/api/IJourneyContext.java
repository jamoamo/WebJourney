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
 * The context of a specific browser journey.
 * @author James Amoore
 */
public interface IJourneyContext
{
	
	/**
	 * Get the browser for the current journey.
	 * @return the browser.
	 */
	IBrowser getBrowser();
	
	/**
	 * Sets an input value for the journey.
	 * 
	 * @param inputType The type of Input
	 * @param inputValue The value for the input
	 */
	void setJourneyInput(String inputType, Object inputValue);
	
	/**
	 * Gets an input value for the journey.
	 * 
	 * @param inputType The type of Input
	 * @return the input value.
	 */
	Object getJourneyInput(String inputType);
	
	/**
	 * Gets all the observers of the journey.
	 * @return the journey observers.
	 */
	List<IJourneyObserver> getJourneyObservers();
	
	/**
	 * Sets all the observers of the journey.
	 * @param observers The journey observers
	 */
	void setJourneyObservers(List<IJourneyObserver> observers);
}

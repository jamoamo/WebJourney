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

import io.github.jamoamo.webjourney.api.AWebAction;
import java.util.Collections;
import java.util.List;
import io.github.jamoamo.webjourney.api.IWebJourneyPath;

/**
 * Represents a defined sequence of web actions that form a journey.
 * 
 * <p>A {@code WebJourney} is an immutable object that encapsulates a series of
 * {@link io.github.jamoamo.webjourney.api.AWebAction}s to be executed by a
 * {@link WebTraveller}. It provides the structure for navigating and interacting
 * with web pages.</p>
 * 
 * @author James Amoore
 * @see IWebJourneyPath
 * @see WebTraveller
 * @see io.github.jamoamo.webjourney.api.AWebAction
 * @since 1.0.0
 */
public final class WebJourney implements IWebJourneyPath
{
	private final List<AWebAction> journeyActions;
	
	/**
	 * Constructs a new WebJourney with the given list of actions.
	 * @param journeyActions The list of actions that comprise this journey.
	 * @since 1.0.0
	 */
	protected WebJourney(List<AWebAction> journeyActions)
	{
		this.journeyActions = journeyActions;
	}
	
	/**
	 * Returns an unmodifiable list of actions in this journey.
	 * @return An unmodifiable list of {@link io.github.jamoamo.webjourney.api.AWebAction}s.
	 * @since 1.0.0
	 */
	@Override
	public List<AWebAction> getActions()
	{
		return Collections.unmodifiableList(this.journeyActions);
	}
}

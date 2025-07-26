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

import java.util.stream.Stream;

/**
 * Represents a series of steps (crumbs) that have been performed during a web journey.
 * This interface provides mechanisms to track the execution path of a journey, useful for debugging and logging.
 * 
 * @author James Amoore
 * @see ICrumb
 * @since 1.0.0
 */
public interface IJourneyBreadcrumb
{
	/**
	 * Adds a new crumb to the end of the breadcrumb trail, marking a step in the journey.
	 * @param crumb The {@link ICrumb} to add to the breadcrumb.
	 * @since 1.0.0
	 */
	void pushCrumb(ICrumb crumb);
	
	/**
	 * Removes the most recently added crumb from the breadcrumb trail.
	 * This is typically called when an action or step in the journey completes.
	 * @since 1.0.0
	 */
	void popCrumb();
	
	/**
	 * Returns a stream of all crumbs currently in the breadcrumb trail, in order of their addition.
	 * @return A {@link Stream} of {@link ICrumb} objects representing the journey's path.
	 * @since 1.0.0
	 */
	Stream<ICrumb> getCrumbStream();
}

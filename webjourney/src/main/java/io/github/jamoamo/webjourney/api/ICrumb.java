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

/**
 * Represents an item that can be included in the journey breadcrumb. This interface is used
 * to provide clear, traceable steps throughout a web journey's execution, aiding in debugging and monitoring.
 * 
 * @author James Amoore
 * @see IJourneyBreadcrumb
 * @since 1.0.0
 */
public interface ICrumb
{
	/**
	 * Returns the descriptive name of this crumb. This name provides specific details about the action or state.
	 * @return The name of the crumb.
	 * @since 1.0.0
	 */
	String getCrumbName();
	
	/**
	 * Returns the category or type of this crumb. This categorizes the crumb (e.g., "Action", "Page").
	 * @return The category crumb.
	 * @since 1.0.0
	 */
	String getCrumbType();
}

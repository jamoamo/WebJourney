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
package io.github.jamoamo.webjourney.reserved;

import io.github.jamoamo.webjourney.api.ICrumb;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import java.util.Stack;
import java.util.stream.Stream;

/**
 * A Journey breadcrumb.
 * 
 * @author James Amoore
 */
public class JourneyBreadcrumb implements IJourneyBreadcrumb
{
	private final Stack<ICrumb> breadcrumbs = new Stack<>();
	
	/**
	 * Adds a crumb to the breadcrumb.
	 * @param crumb the crumb to add
	 */
	@Override
	public void pushCrumb(ICrumb crumb)
	{
		this.breadcrumbs.push(crumb);
	}

	/**
	 * Removes the last crumb from the breadcrumb.
	 */
	@Override
	public void popCrumb()
	{
		if(this.breadcrumbs.isEmpty())
		{
			throw new BreadcrumbException("Can't remove crumb. There are no crumbs.");
		}
		this.breadcrumbs.pop();
	}

	/**
	 * Returns the crumbs in a stream.
	 * @return the crumb stream
	 */
	@Override
	public Stream<ICrumb> getCrumbStream()
	{
		return this.breadcrumbs.stream();
	}
}

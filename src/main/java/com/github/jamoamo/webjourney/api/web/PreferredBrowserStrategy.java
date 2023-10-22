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
package com.github.jamoamo.webjourney.api.web;

/**
 *	Uses the supplied browser as the preferred browser to use.
 * @author James Amoore
 */
public final class PreferredBrowserStrategy implements IPreferredBrowserStrategy
{
	private final IBrowserFactory browserFactory;
	
	/**
	 * Use a browser instance of the provided standard browser.
	 * @param standardBrowser the browser to use an instance of.
	 */
	public PreferredBrowserStrategy(StandardBrowser standardBrowser)
	{
		this.browserFactory = standardBrowser.getBrowserFactory();
	}
	
	/**
	 * Generate a browser instance using the provided browser factory.
	 * @param browserFactory the browser factory to use.
	 */
	public PreferredBrowserStrategy(IBrowserFactory browserFactory)
	{
		this.browserFactory = browserFactory;
	}
	
	/**
	 * return the browser instance that should be used.
	 * @return the browser to use.
	 */
	@Override
	public IBrowser getPreferredBrowser(IBrowserOptions options)
	{
		return this.browserFactory.createBrowser(options);
	}
	
}

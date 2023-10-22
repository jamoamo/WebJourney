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

import java.net.URL;
import java.util.Arrays;

/**
 * Uses a prioritized list of browsers to determine the browser to use. 
 * Iterates the list until it finds a browser that will work on the system.
 * @author James Amoore
 */
public class PriorityBrowserStrategy implements IPreferredBrowserStrategy
{
	private final IBrowserFactory[] browserPriority;
	
	/**
	 * Create a new instance using the default browser priority.
	 */
	public PriorityBrowserStrategy()
	{
		this.browserPriority = 
				  (IBrowserFactory[])Arrays
							 .stream(StandardBrowser.values())
							 .map(standard -> standard.getBrowserFactory())
							 .toList().toArray(new IBrowserFactory[0]);
	}
	
	/**
	 * Create a new instance using the provided priority of standard browsers.
	 * @param browsers Prioritized browser list.
	 */
	public PriorityBrowserStrategy(StandardBrowser[] browsers)
	{
		this.browserPriority = 
				  (IBrowserFactory[])Arrays
							 .stream(browsers)
							 .map(standard -> standard.getBrowserFactory())
							 .toList().toArray(new IBrowserFactory[0]);
	}
	
	/**
	 * Create a new instance using the provided list of browser factories.
	 * @param browserFactories prioritized browser factory list.
	 */
	public PriorityBrowserStrategy(IBrowserFactory[] browserFactories)
	{
		this.browserPriority = browserFactories;
	}
	
	/**
	 * Returns the highest priority browser for the system.
	 */
	@Override
	@SuppressWarnings("IllegalCatch")
	public IBrowser getPreferredBrowser(IBrowserOptions options)
	{
		for(IBrowserFactory browserFactory : this.browserPriority)
		{
			try
			{
				IBrowser browser = browserFactory.createBrowser(options);
				//test the browser
				browser.navigateToUrl(new URL("http://www.google.com"));
				return browser;
			}
			catch(Exception ex)
			{
				//browser failed
			}
		}
		
		
		throw new RuntimeException("Could not determine suitable browser");
	}
	
}

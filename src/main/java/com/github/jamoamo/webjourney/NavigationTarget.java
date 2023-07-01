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
package com.github.jamoamo.webjourney;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides methods for creating navigation targets.
 * @author James Amoore
 */
public final class NavigationTarget
{
	private NavigationTarget(){}
	
	/**
	 * Creates a navigation target directed at the provided URL.
	 * 
	 * @param url The URL to navigate to
	 * @return a new NavigationTarget instance
	 */
	public static ANavigationTarget toUrl(URL url)
	{
		return new UrlNavigationTarget(url);
	}
	
	/**
	 * Creates a navigation target directed at the provided URL.
	 * 
	 * @param url The URL to navigate to
	 * @return a new NavigationTarget instance
	 * @throws java.net.MalformedURLException if the provided URL is invalid.
	 */
	public static ANavigationTarget toUrl(String url)
			  throws MalformedURLException
	{
		return new UrlNavigationTarget(new URL(url));
	}
	
	/**
	 * Creates a navigation target directed at the previous page.
	 * 
	 * @return a new NavigationTarget instance
	 */
	public static ANavigationTarget back()
	{
		return new BackNavigationTarget();
	}
	
	/**
	 * Creates a navigation target directed at the previous page.
	 * 
	 * @return a new NavigationTarget instance
	 */
	public static ANavigationTarget forward()
	{
		return new ForwardNavigationTarget();
	}
	
	/**
	 * Creates a navigation target directed at the previous page.
	 * 
	 * @return a new NavigationTarget instance
	 */
	public static ANavigationTarget refresh()
	{
		return new RefreshNavigationTarget();
	}
}

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
package io.github.jamoamo.webjourney.test.mock;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A simple router for mapping URLs to mock pages or page providers.
 */
public final class MockRouter
{
	private final Map<String, Function<URL, MockWebPage>> urlToProvider = new ConcurrentHashMap<>();

	/**
	 * Route a specific URL to a fixed page instance.
	 * @param url the URL string
	 * @param page the page to return when the url is requested
	 * @return this router for chaining
	 */
	public MockRouter route(String url, MockWebPage page)
	{
		this.urlToProvider.put(url, u -> page);
		return this;
	}

	/**
	 * Route a specific URL to a page provider function.
	 * @param url the URL string
	 * @param provider provider that receives the requested URL and returns a page
	 * @return this router for chaining
	 */
	public MockRouter route(String url, Function<URL, MockWebPage> provider)
	{
		this.urlToProvider.put(url, provider);
		return this;
	}

	/**
	 * Resolve a URL to a MockWebPage if one is configured.
	 * @param url the url
	 * @return the page or null if no mapping exists
	 */
	public MockWebPage resolve(URL url)
	{
		Function<URL, MockWebPage> provider = this.urlToProvider.get(url.toString());
		if(provider == null)
		{
			return null;
		}
		return provider.apply(url);
	}
}
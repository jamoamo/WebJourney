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

import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.IWebPage;
import io.github.jamoamo.webjourney.api.web.XNavigationError;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Mock implementation of IBrowserWindow with in-memory history and router.
 */
final class MockBrowserWindow implements IBrowserWindow
{
	private final String name;
	private final MockRouter router;

	private boolean active = false;
	private MockWebPage currentPage;
	private URL currentUrl;

	private final Deque<URL> backStack = new ArrayDeque<>();
	private final Deque<URL> forwardStack = new ArrayDeque<>();

	MockBrowserWindow(String name, MockRouter router)
	{
		this.name = name;
		this.router = router;
	}

	void setActive(boolean active)
	{
		this.active = active;
	}

	boolean isActive()
	{
		return this.active;
	}

	@Override
	public String getCurrentUrl() throws XWebException
	{
		ensureActive();
		return this.currentUrl == null ? null : this.currentUrl.toString();
	}

	@Override
	public IWebPage getCurrentPage() throws XWebException
	{
		ensureActive();
		return this.currentPage;
	}

	@Override
	public IWebPage refreshCurrentPage() throws XWebException
	{
		ensureActive();
		if(this.currentUrl == null)
		{
			return this.currentPage;
		}
		MockWebPage page = this.router.resolve(this.currentUrl);
		if(page == null)
		{
			throw new XNavigationError("No page for URL: " + this.currentUrl);
		}
		this.currentPage = page;
		return this.currentPage;
	}

	@Override
	public void close() throws XWebException
	{
		ensureActive();
		this.active = false;
	}

	@Override
	public String getName() throws XWebException
	{
		return this.name;
	}

	@Override
	public String getTitle()
	{
		return this.currentPage == null ? null : this.currentPage.getTitle();
	}

	@Override
	public IWebPage navigateToUrl(URL url) throws XNavigationError
	{
		ensureActive();
		MockWebPage page = this.router.resolve(url);
		if(page == null)
		{
			throw new XNavigationError("No page for URL: " + url);
		}
		if(this.currentUrl != null)
		{
			this.backStack.push(this.currentUrl);
		}
		this.currentUrl = url;
		this.forwardStack.clear();
		this.currentPage = page;
		return this.currentPage;
	}

	@Override
	public IWebPage navigateBack() throws XNavigationError
	{
		ensureActive();
		if(this.backStack.isEmpty())
		{
			throw new XNavigationError("No back history.");
		}
		this.forwardStack.push(this.currentUrl);
		this.currentUrl = this.backStack.pop();
		MockWebPage page = this.router.resolve(this.currentUrl);
		if(page == null)
		{
			throw new XNavigationError("No page for URL: " + this.currentUrl);
		}
		this.currentPage = page;
		return this.currentPage;
	}

	@Override
	public IWebPage navigateForward() throws XNavigationError
	{
		ensureActive();
		if(this.forwardStack.isEmpty())
		{
			throw new XNavigationError("No forward history.");
		}
		this.backStack.push(this.currentUrl);
		this.currentUrl = this.forwardStack.pop();
		MockWebPage page = this.router.resolve(this.currentUrl);
		if(page == null)
		{
			throw new XNavigationError("No page for URL: " + this.currentUrl);
		}
		this.currentPage = page;
		return this.currentPage;
	}

	void loadInitial(String url) throws MalformedURLException, XNavigationError
	{
		this.currentUrl = url == null ? null : new URL(url);
		if(this.currentUrl != null)
		{
			MockWebPage page = this.router.resolve(this.currentUrl);
			if(page == null)
			{
				throw new XNavigationError("No page for URL: " + this.currentUrl);
			}
			this.currentPage = page;
		}
	}

	private void ensureActive() throws XWebException
	{
		if(!this.active)
		{
			throw new XWebException("Window is inactive.");
		}
	}
}
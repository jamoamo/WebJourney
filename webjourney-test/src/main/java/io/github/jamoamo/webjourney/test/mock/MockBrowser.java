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

import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserWindow;
import io.github.jamoamo.webjourney.api.web.SessionCookie;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mock implementation of IBrowser managing windows and a router.
 */
public final class MockBrowser implements IBrowser
{
	private final MockRouter router;
	private final Map<String, MockBrowserWindow> windows = new LinkedHashMap<>();
	private final Map<CookieKey, SessionCookie> cookies = new LinkedHashMap<>();

	/**
	 * Identifies a stored cookie by its name, domain and path, avoiding the ambiguity of a delimited string key.
	 */
	private static record CookieKey(String name, String domain, String path)
	{
	}

	public MockBrowser(MockRouter router)
	{
		this.router = router == null ? new MockRouter() : router;
		String main = UUID.randomUUID().toString();
		MockBrowserWindow window = new MockBrowserWindow(main, this.router);
		window.setActive(true);
		this.windows.put(main, window);
	}

	public MockRouter getRouter()
	{
		return this.router;
	}

	public void loadInitial(String url) throws XWebException, MalformedURLException
	{
		MockBrowserWindow window = (MockBrowserWindow) getActiveWindow();
		window.loadInitial(url);
	}

	@Override
	public IBrowserWindow getActiveWindow() throws XWebException
	{
		return this.windows.values().stream().filter(MockBrowserWindow::isActive).findFirst().orElse(null);
	}

	@Override
	public IBrowserWindow switchToWindow(String windowName) throws XWebException
	{
		this.windows.values().forEach(w -> w.setActive(false));
		MockBrowserWindow window = this.windows.get(windowName);
		if(window != null)
		{
			window.setActive(true);
		}
		return window;
	}

	@Override
	public IBrowserWindow openNewWindow() throws XWebException
	{
		this.windows.values().forEach(w -> w.setActive(false));
		String name = UUID.randomUUID().toString();
		MockBrowserWindow window = new MockBrowserWindow(name, this.router);
		this.windows.put(name, window);
		window.setActive(true);
		return window;
	}

	/**
	 * Returns the stored cookies visible to the active window's current URL (domain, path, secure and expiry
	 * are honoured). Cookie values are credentials and are never logged by this class.
	 */
	@Override
	public List<SessionCookie> getCookies() throws XWebException
	{
		URL current = currentUrl();
		if(current == null)
		{
			return new ArrayList<>();
		}
		Instant now = Instant.now();
		return this.cookies.values().stream()
			.filter(c -> !c.isExpired(now) && isVisible(c, current))
			.collect(Collectors.toList());
	}

	/**
	 * Stores a cookie, enforcing the same rule as a real browser: the active window must currently be on a page
	 * whose host matches the cookie's domain, otherwise an {@link XWebException} (carrying name and domain only)
	 * is thrown.
	 */
	@Override
	public void addCookie(SessionCookie cookie) throws XWebException
	{
		Objects.requireNonNull(cookie, "cookie");
		URL current = currentUrl();
		if(current == null || !domainMatches(cookie.getDomain(), current.getHost()))
		{
			throw new XWebException(String.format("Failed to add cookie [%s] for domain [%s]: the browser is not on a "
				+ "page in that domain.", cookie.getName(), cookie.getDomain()));
		}
		CookieKey key = new CookieKey(cookie.getName(), cookie.getDomain(), cookie.getPath());
		if(cookie.isExpired(Instant.now()))
		{
			this.cookies.remove(key);
		}
		else
		{
			this.cookies.put(key, cookie);
		}
	}

	/**
	 * Deletes the stored cookies visible to the active window's current URL.
	 */
	@Override
	public void deleteAllCookies() throws XWebException
	{
		URL current = currentUrl();
		if(current != null)
		{
			this.cookies.values().removeIf(c -> isVisible(c, current));
		}
	}

	private URL currentUrl() throws XWebException
	{
		IBrowserWindow window = getActiveWindow();
		String url = window == null ? null : window.getCurrentUrl();
		if(url == null)
		{
			return null;
		}
		try
		{
			return new URL(url);
		}
		catch(MalformedURLException ex)
		{
			throw new XWebException("The current URL is malformed.");
		}
	}

	private static boolean isVisible(SessionCookie cookie, URL url)
	{
		if(!domainMatches(cookie.getDomain(), url.getHost()))
		{
			return false;
		}
		if(cookie.isSecure() && !"https".equalsIgnoreCase(url.getProtocol()))
		{
			return false;
		}
		String requestPath = url.getPath().isEmpty() ? "/" : url.getPath();
		String cookiePath = cookie.getPath();
		return requestPath.equals(cookiePath)
			|| requestPath.startsWith(cookiePath.endsWith("/") ? cookiePath : cookiePath + "/");
	}

	/**
	 * A cookie domain with a leading dot matches the bare domain and all its subdomains; one without is host-only.
	 */
	private static boolean domainMatches(String cookieDomain, String host)
	{
		String h = host.toLowerCase(Locale.ROOT);
		String d = cookieDomain.toLowerCase(Locale.ROOT);
		if(d.startsWith("."))
		{
			String bare = d.substring(1);
			return h.equals(bare) || h.endsWith(d);
		}
		return h.equals(d);
	}

	@Override
	public boolean supportsCookies()
	{
		return true;
	}

	@Override
	public void exit()
	{
		this.windows.clear();
	}
}
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
package io.github.jamoamo.webjourney.api.web;

import java.util.List;

/**
 * A browser instance.
 *
 * @author James Amoore
 */
public interface IBrowser
{
	
	/**
	 * Returns the current active window. This is the first window to have been opened for the browser.
	 * 
	 * @return the main browser window.
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if a browsing error occurs
	 */
	IBrowserWindow getActiveWindow() throws XWebException;
	
	/**
	 * Switches to the browser window with the given name. 
	 * If that window doesn't exist or has subsequently been closed then null is returned and then no window 
	 * is set as the active window.
	 * 
	 * @param windowName the name of the window
	 * @return the main browser window.
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if a browsing error occurs
	 */
	IBrowserWindow switchToWindow(String windowName) throws XWebException;
	
	/**
	 * Open a new browser window.
	 * @return the openedWindow
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if a browsing error occurs
	 */
	IBrowserWindow openNewWindow() throws XWebException;

	/**
	 * Returns the cookies the browser would send for the current page's URL. Cookies for other URLs and domains
	 * are not visible. HttpOnly cookies are included.
	 * <p>
	 * <b>Cookie values are credentials and must never be logged or put in exception messages.</b>
	 * <p>
	 * A returned cookie's domain may be the literal placeholder value {@code "unknown"} when the underlying driver
	 * did not report one. Such a cookie should not be passed back to {@link #addCookie(SessionCookie)}.
	 *
	 * @return the visible cookies, never null
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if a browsing error occurs
	 * @throws UnsupportedOperationException if this browser does not support cookie access
	 * @since the next release
	 */
	default List<SessionCookie> getCookies() throws XWebException
	{
		throw new UnsupportedOperationException("This browser does not support cookie access.");
	}

	/**
	 * Adds a cookie to the browser. The browser must already be on a page whose domain matches the cookie's
	 * domain (a domain-wide cookie such as ".example.com" can be added from any page on example.com), otherwise
	 * the browser rejects it and an {@link XWebException} is thrown.
	 * <p>
	 * A browser may also silently discard a cookie it considers invalid for the page (Chrome does this for a Secure
	 * cookie added while on plain http), so a successful call does not guarantee the cookie is visible; use
	 * {@link #getCookies()} to confirm.
	 * <p>
	 * <b>Cookie values are credentials and must never be logged or put in exception messages.</b> The exception
	 * thrown here carries the cookie's name and domain only.
	 * <p>
	 * Do not pass a cookie whose domain is the {@code "unknown"} placeholder described in {@link #getCookies()};
	 * such a cookie did not report a real domain and is not safe to replay.
	 *
	 * @param cookie the cookie to add
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if the browser rejects the cookie
	 * @throws UnsupportedOperationException if this browser does not support cookie access
	 * @since the next release
	 */
	default void addCookie(SessionCookie cookie) throws XWebException
	{
		throw new UnsupportedOperationException("This browser does not support cookie access.");
	}

	/**
	 * Deletes all the cookies visible to the current page's URL.
	 *
	 * @throws io.github.jamoamo.webjourney.api.web.XWebException if a browsing error occurs
	 * @throws UnsupportedOperationException if this browser does not support cookie access
	 * @since the next release
	 */
	default void deleteAllCookies() throws XWebException
	{
		throw new UnsupportedOperationException("This browser does not support cookie access.");
	}

	/**
	 * Whether this browser supports cookie access ({@link #getCookies()}, {@link #addCookie(SessionCookie)} and
	 * {@link #deleteAllCookies()}). Callers should check this before calling those methods instead of relying on
	 * catching {@link UnsupportedOperationException}.
	 *
	 * @return true if the cookie methods are supported, false if they throw {@link UnsupportedOperationException}
	 * @since the next release
	 */
	default boolean supportsCookies()
	{
		return false;
	}

	/**
	 * Exit the browser.
	 */
	void exit();
}

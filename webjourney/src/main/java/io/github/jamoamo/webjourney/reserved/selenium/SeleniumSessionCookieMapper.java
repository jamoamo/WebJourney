/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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
package io.github.jamoamo.webjourney.reserved.selenium;

import io.github.jamoamo.webjourney.api.web.SessionCookie;
import io.github.jamoamo.webjourney.api.web.SessionCookie.SameSite;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import org.openqa.selenium.Cookie;

/**
 * Maps between Selenium cookies and {@link SessionCookie}s. Nothing here logs, and no exception raised here
 * carries a cookie value.
 *
 * @author James Amoore
 */
final class SeleniumSessionCookieMapper
{
	private SeleniumSessionCookieMapper()
	{
	}

	/**
	 * Maps a Selenium {@link Cookie} to a {@link SessionCookie}.
	 *
	 * @param cookie the Selenium cookie to map
	 * @return the equivalent {@link SessionCookie}
	 */
	static SessionCookie fromSelenium(Cookie cookie)
	{
		Date expiry = cookie.getExpiry();
		// Selenium reports a null domain for some drivers; fall back to a placeholder rather than fail the read.
		String domain = cookie.getDomain() == null || cookie.getDomain().isBlank() ? "unknown" : cookie.getDomain();
		return SessionCookie.builder(cookie.getName(), cookie.getValue() == null ? "" : cookie.getValue(), domain)
			.withPath(cookie.getPath())
			.withExpiry(expiry == null ? null : expiry.toInstant())
			.withSecure(cookie.isSecure())
			.withHttpOnly(cookie.isHttpOnly())
			.withSameSite(sameSiteFromString(cookie.getSameSite()))
			.build();
	}

	/**
	 * Maps a {@link SessionCookie} to a Selenium {@link Cookie}.
	 *
	 * @param cookie the session cookie to map
	 * @return the equivalent Selenium {@link Cookie}
	 */
	static Cookie toSelenium(SessionCookie cookie)
	{
		Instant expiry = cookie.getExpiry();
		Cookie.Builder builder = new Cookie.Builder(cookie.getName(), cookie.getValue())
			.domain(cookie.getDomain())
			.path(cookie.getPath())
			.isSecure(cookie.isSecure())
			.isHttpOnly(cookie.isHttpOnly());
		if(expiry != null)
		{
			builder.expiresOn(Date.from(expiry));
		}
		if(cookie.getSameSite() != null)
		{
			builder.sameSite(sameSiteToString(cookie.getSameSite()));
		}
		return builder.build();
	}

	/**
	 * Maps Selenium's string SameSite representation to a {@link SameSite}. An unrecognized value (including one
	 * that doesn't match "none", "lax" or "strict") maps to {@code null} rather than failing the read.
	 *
	 * @param value the Selenium SameSite string, or null
	 * @return the equivalent {@link SameSite}, or null if {@code value} is null or unrecognized
	 */
	static SameSite sameSiteFromString(String value)
	{
		if(value == null)
		{
			return null;
		}
		switch(value.toLowerCase(Locale.ROOT))
		{
			case "none":
				return SameSite.NONE;
			case "lax":
				return SameSite.LAX;
			case "strict":
				return SameSite.STRICT;
			default:
				return null;
		}
	}

	/**
	 * Maps a {@link SameSite} to Selenium's string representation.
	 *
	 * @param sameSite the SameSite value to map, not null
	 * @return the equivalent Selenium SameSite string
	 * @throws IllegalStateException if {@code sameSite} is an enum value not handled by this mapping
	 */
	static String sameSiteToString(SameSite sameSite)
	{
		switch(sameSite)
		{
			case NONE:
				return "None";
			case LAX:
				return "Lax";
			case STRICT:
				return "Strict";
			default:
				throw new IllegalStateException("Unhandled SameSite value: " + sameSite);
		}
	}
}

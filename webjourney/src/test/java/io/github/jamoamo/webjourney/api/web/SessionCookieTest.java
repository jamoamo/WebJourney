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
package io.github.jamoamo.webjourney.api.web;

import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SessionCookie}.
 */
public class SessionCookieTest
{
	private static final String SECRET = "s3cr3t-value";

	@Test
	public void toStringNeverContainsValue()
	{
		SessionCookie cookie = SessionCookie.builder("pigeon_access", SECRET, ".example.com").build();
		assertFalse(cookie.toString().contains(SECRET));
		assertTrue(cookie.toString().contains("pigeon_access"));
		assertTrue(cookie.toString().contains(".example.com"));
	}

	@Test
	public void equalsAndHashCodeUseValueButDoNotPrintIt()
	{
		SessionCookie a = SessionCookie.builder("n", "one", "d.test").build();
		SessionCookie same = SessionCookie.builder("n", "one", "d.test").build();
		SessionCookie changed = SessionCookie.builder("n", "two", "d.test").build();
		assertEquals(a, same);
		assertEquals(a.hashCode(), same.hashCode());
		assertNotEquals(a, changed);
	}

	private static SessionCookie baseline()
	{
		return SessionCookie.builder("n", "v", "d.test")
			.withPath("/p")
			.withExpiry(Instant.parse("2030-01-01T00:00:00Z"))
			.withSecure(true)
			.withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX)
			.build();
	}

	@Test
	public void equalsComparesEveryField()
	{
		SessionCookie base = baseline();
		assertEquals(base, baseline());

		assertNotEquals(base, SessionCookie.builder("other", "v", "d.test")
			.withPath("/p").withExpiry(base.getExpiry()).withSecure(true).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "name should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "other.test")
			.withPath("/p").withExpiry(base.getExpiry()).withSecure(true).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "domain should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "d.test")
			.withPath("/other").withExpiry(base.getExpiry()).withSecure(true).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "path should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "d.test")
			.withPath("/p").withExpiry(base.getExpiry().plusSeconds(1)).withSecure(true).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "expiry should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "d.test")
			.withPath("/p").withExpiry(base.getExpiry()).withSecure(false).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "secure should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "d.test")
			.withPath("/p").withExpiry(base.getExpiry()).withSecure(true).withHttpOnly(false)
			.withSameSite(SessionCookie.SameSite.LAX).build(), "httpOnly should be compared");

		assertNotEquals(base, SessionCookie.builder("n", "v", "d.test")
			.withPath("/p").withExpiry(base.getExpiry()).withSecure(true).withHttpOnly(true)
			.withSameSite(SessionCookie.SameSite.STRICT).build(), "sameSite should be compared");

		assertNotEquals(base, null);
		assertNotEquals(base, "not a cookie");
	}

	@Test
	public void defaultsAreSessionCookieWithRootPath()
	{
		SessionCookie cookie = SessionCookie.builder("n", "v", "d.test").build();
		assertEquals("/", cookie.getPath());
		assertNull(cookie.getExpiry());
		assertNull(cookie.getSameSite());
		assertFalse(cookie.isSecure());
		assertFalse(cookie.isHttpOnly());
		assertFalse(cookie.isExpired(Instant.MAX));
	}

	@Test
	public void blankPathFallsBackToRoot()
	{
		assertEquals("/", SessionCookie.builder("n", "v", "d.test").withPath(" ").build().getPath());
		assertEquals("/", SessionCookie.builder("n", "v", "d.test").withPath(null).build().getPath());
	}

	@Test
	public void isExpiredComparesAgainstGivenInstant()
	{
		Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
		SessionCookie cookie = SessionCookie.builder("n", "v", "d.test").withExpiry(expiry).build();
		assertFalse(cookie.isExpired(expiry.minusSeconds(1)));
		assertTrue(cookie.isExpired(expiry));
		assertTrue(cookie.isExpired(expiry.plusSeconds(1)));
	}

	@Test
	public void rejectsInvalidArgumentsWithoutLeakingValue()
	{
		assertThrows(IllegalArgumentException.class, () -> SessionCookie.builder(" ", SECRET, "d.test"));
		assertThrows(IllegalArgumentException.class, () -> SessionCookie.builder(null, SECRET, "d.test"));
		IllegalArgumentException ex =
			assertThrows(IllegalArgumentException.class, () -> SessionCookie.builder("n", SECRET, ""));
		assertFalse(ex.getMessage().contains(SECRET));
		assertThrows(IllegalArgumentException.class, () -> SessionCookie.builder("n", SECRET, null));
		assertThrows(IllegalArgumentException.class, () -> SessionCookie.builder("n", null, "d.test"));
	}

	@Test
	public void ibrowserDefaultsThrowUnsupported()
	{
		IBrowser browser = new IBrowser()
		{
			@Override
			public IBrowserWindow getActiveWindow()
			{
				return null;
			}

			@Override
			public IBrowserWindow switchToWindow(String windowName)
			{
				return null;
			}

			@Override
			public IBrowserWindow openNewWindow()
			{
				return null;
			}

			@Override
			public void exit()
			{
			}
		};
		SessionCookie cookie = SessionCookie.builder("n", "v", "d.test").build();
		assertThrows(UnsupportedOperationException.class, browser::getCookies);
		assertThrows(UnsupportedOperationException.class, () -> browser.addCookie(cookie));
		assertThrows(UnsupportedOperationException.class, browser::deleteAllCookies);
	}
}

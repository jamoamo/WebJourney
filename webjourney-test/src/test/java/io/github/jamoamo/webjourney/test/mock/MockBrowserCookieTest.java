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
package io.github.jamoamo.webjourney.test.mock;

import io.github.jamoamo.webjourney.api.web.SessionCookie;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the cookie support of {@link MockBrowser}, including its enforcement of the domain rule.
 */
public class MockBrowserCookieTest
{
	private static final String SECRET = "s3cr3t-value";

	private MockBrowser browser;

	@BeforeEach
	public void setUp() throws Exception
	{
		MockRouter router = new MockRouter();
		router.route("https://www.example.com/home", MockWebPage.root(MockElement.tag("html")));
		router.route("https://www.example.com/deep/page", MockWebPage.root(MockElement.tag("html")));
		router.route("https://other.test/home", MockWebPage.root(MockElement.tag("html")));
		this.browser = new MockBrowser(router);
	}

	private void go(String url) throws Exception
	{
		this.browser.getActiveWindow().navigateToUrl(new URL(url));
	}

	private static SessionCookie cookie(String domain)
	{
		return SessionCookie.builder("auth", SECRET, domain).build();
	}

	@Test
	public void getCookiesAndDeleteAllCookiesBeforeAnyNavigationDoNotThrow() throws Exception
	{
		assertEquals(0, this.browser.getCookies().size());
		this.browser.deleteAllCookies();
		assertTrue(this.browser.getCookies().isEmpty());
	}

	@Test
	public void addCookieBeforeNavigatingThrows()
	{
		XWebException ex = assertThrows(XWebException.class, () -> this.browser.addCookie(cookie(".example.com")));
		assertFalse(ex.getMessage().contains(SECRET));
	}

	@Test
	public void addCookieForAnotherDomainThrowsWithoutLeakingValue() throws Exception
	{
		go("https://other.test/home");
		XWebException ex = assertThrows(XWebException.class, () -> this.browser.addCookie(cookie(".example.com")));
		assertTrue(ex.getMessage().contains("auth"));
		assertTrue(ex.getMessage().contains(".example.com"));
		assertFalse(ex.getMessage().contains(SECRET));
	}

	@Test
	public void domainWideCookieIsVisibleOnMatchingHostOnly() throws Exception
	{
		go("https://www.example.com/home");
		this.browser.addCookie(cookie(".example.com"));
		assertEquals(1, this.browser.getCookies().size());
		assertEquals(SECRET, this.browser.getCookies().get(0).getValue());

		go("https://other.test/home");
		assertTrue(this.browser.getCookies().isEmpty());
	}

	@Test
	public void hostOnlyCookieDoesNotMatchParentDomain() throws Exception
	{
		go("https://www.example.com/home");
		assertThrows(XWebException.class, () -> this.browser.addCookie(cookie("example.com")));
	}

	@Test
	public void pathAndSecureRulesApply() throws Exception
	{
		go("https://www.example.com/home");
		this.browser.addCookie(SessionCookie.builder("scoped", "v", ".example.com").withPath("/deep").build());
		assertTrue(this.browser.getCookies().isEmpty());
		go("https://www.example.com/deep/page");
		assertEquals(1, this.browser.getCookies().size());
	}

	@Test
	public void expiredCookieIsNotReturned() throws Exception
	{
		go("https://www.example.com/home");
		this.browser.addCookie(SessionCookie.builder("old", "v", ".example.com")
			.withExpiry(Instant.now().minus(1, ChronoUnit.HOURS)).build());
		assertTrue(this.browser.getCookies().isEmpty());
	}

	@Test
	public void addingSameCookieReplacesIt() throws Exception
	{
		go("https://www.example.com/home");
		this.browser.addCookie(SessionCookie.builder("auth", "one", ".example.com").build());
		this.browser.addCookie(SessionCookie.builder("auth", "two", ".example.com").build());
		List<SessionCookie> cookies = this.browser.getCookies();
		assertEquals(1, cookies.size());
		assertEquals("two", cookies.get(0).getValue());
	}

	@Test
	public void deleteAllCookiesRemovesVisibleCookies() throws Exception
	{
		go("https://www.example.com/home");
		this.browser.addCookie(cookie(".example.com"));
		this.browser.deleteAllCookies();
		assertTrue(this.browser.getCookies().isEmpty());
	}
}

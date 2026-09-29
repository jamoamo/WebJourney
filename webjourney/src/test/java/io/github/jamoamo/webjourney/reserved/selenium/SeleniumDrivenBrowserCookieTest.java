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
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.UnableToSetCookieException;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the cookie methods of {@link SeleniumDrivenBrowser} and {@link SeleniumSessionCookieMapper},
 * against a mocked driver. The real-Chrome behaviour is covered by {@link SeleniumCookieIntegrationTest}.
 */
public class SeleniumDrivenBrowserCookieTest
{
	private static final String SECRET = "s3cr3t-value";

	private Options options;
	private SeleniumDrivenBrowser browser;

	@BeforeEach
	public void setUp()
	{
		RemoteWebDriver driver = Mockito.mock(RemoteWebDriver.class);
		this.options = Mockito.mock(Options.class);
		Capabilities capabilities = Mockito.mock(Capabilities.class);
		Mockito.when(capabilities.getBrowserName()).thenReturn("Test Browser");
		Mockito.when(capabilities.getBrowserVersion()).thenReturn("1.0");
		Mockito.when(this.options.timeouts()).thenReturn(Mockito.mock(Timeouts.class));
		Mockito.when(driver.manage()).thenReturn(this.options);
		Mockito.when(driver.getCapabilities()).thenReturn(capabilities);
		this.browser = new SeleniumDrivenBrowser(driver);
	}

	private static String fullText(Throwable t)
	{
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	@Test
	public void getCookiesMapsAllFields() throws Exception
	{
		Date expiry = new Date(1_900_000_000_000L);
		Cookie httpOnly = new Cookie.Builder("a", "1").domain(".x.test").path("/p").expiresOn(expiry)
			.isSecure(true).isHttpOnly(true).sameSite("Strict").build();
		Cookie session = new Cookie.Builder("b", "2").domain("x.test").build();
		Mockito.when(this.options.getCookies()).thenReturn(Set.of(httpOnly, session));

		List<SessionCookie> cookies = this.browser.getCookies();

		assertEquals(2, cookies.size());
		SessionCookie a = cookies.stream().filter(c -> c.getName().equals("a")).findFirst().orElseThrow();
		assertEquals(".x.test", a.getDomain());
		assertEquals("/p", a.getPath());
		assertEquals(expiry.toInstant(), a.getExpiry());
		assertTrue(a.isSecure());
		assertTrue(a.isHttpOnly());
		assertEquals(SameSite.STRICT, a.getSameSite());
		SessionCookie b = cookies.stream().filter(c -> c.getName().equals("b")).findFirst().orElseThrow();
		assertNull(b.getExpiry());
		assertNull(b.getSameSite());
	}

	@Test
	public void unknownSameSiteMapsToNull()
	{
		assertNull(SeleniumSessionCookieMapper.sameSiteFromString("Weird"));
		assertNull(SeleniumSessionCookieMapper.sameSiteFromString(null));
		assertEquals(SameSite.LAX, SeleniumSessionCookieMapper.sameSiteFromString("lax"));
	}

	@Test
	public void sameSiteNoneRoundTrips() throws Exception
	{
		Cookie none = new Cookie.Builder("a", "1").domain("x.test").sameSite("None").build();
		Mockito.when(this.options.getCookies()).thenReturn(Set.of(none));

		List<SessionCookie> cookies = this.browser.getCookies();

		assertEquals(1, cookies.size());
		assertEquals(SameSite.NONE, cookies.get(0).getSameSite());
		assertEquals("None", SeleniumSessionCookieMapper.sameSiteToString(SameSite.NONE));
	}

	@Test
	public void nullOrBlankDomainMapsToUnknownPlaceholder()
	{
		Cookie noDomain = Mockito.mock(Cookie.class);
		Mockito.when(noDomain.getName()).thenReturn("a");
		Mockito.when(noDomain.getValue()).thenReturn("1");
		Mockito.when(noDomain.getDomain()).thenReturn(null);
		Mockito.when(noDomain.getPath()).thenReturn("/");

		SessionCookie mapped = SeleniumSessionCookieMapper.fromSelenium(noDomain);

		assertEquals("unknown", mapped.getDomain());
	}

	@Test
	public void nullValueMapsToEmptyStringNotNull()
	{
		Cookie noValue = Mockito.mock(Cookie.class);
		Mockito.when(noValue.getName()).thenReturn("a");
		Mockito.when(noValue.getValue()).thenReturn(null);
		Mockito.when(noValue.getDomain()).thenReturn("x.test");
		Mockito.when(noValue.getPath()).thenReturn("/");

		SessionCookie mapped = SeleniumSessionCookieMapper.fromSelenium(noValue);

		assertEquals("", mapped.getValue());
	}

	@Test
	public void addCookiePassesDomainThroughUnchanged() throws Exception
	{
		Instant expiry = Instant.parse("2030-01-01T00:00:00Z");
		this.browser.addCookie(SessionCookie.builder("n", SECRET, ".x.test").withPath("/z").withExpiry(expiry)
			.withSecure(true).withHttpOnly(true).withSameSite(SameSite.LAX).build());

		ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
		Mockito.verify(this.options).addCookie(captor.capture());
		Cookie sent = captor.getValue();
		assertEquals(".x.test", sent.getDomain());
		assertEquals("/z", sent.getPath());
		assertEquals(Date.from(expiry), sent.getExpiry());
		assertEquals("Lax", sent.getSameSite());
		assertTrue(sent.isSecure());
		assertTrue(sent.isHttpOnly());
	}

	@Test
	public void failedAddNeverLeaksValueInMessageOrStackText()
	{
		Mockito.doThrow(new UnableToSetCookieException("invalid cookie: n=" + SECRET + "; domain=x.test"))
			.when(this.options).addCookie(Mockito.any());

		SessionCookie cookie = SessionCookie.builder("n", SECRET, ".x.test").build();
		XWebException ex = assertThrows(XWebException.class, () -> this.browser.addCookie(cookie));

		assertFalse(ex.getMessage().contains(SECRET));
		assertFalse(fullText(ex).contains(SECRET));
		assertTrue(ex.getMessage().contains("n"));
		assertTrue(ex.getMessage().contains(".x.test"));
		assertNull(ex.getCause());
	}

	@Test
	public void failedReadAndDeleteAreRewrappedWithoutCause()
	{
		Mockito.when(this.options.getCookies()).thenThrow(new RuntimeException("boom " + SECRET));
		Mockito.doThrow(new RuntimeException("boom " + SECRET)).when(this.options).deleteAllCookies();

		XWebException read = assertThrows(XWebException.class, () -> this.browser.getCookies());
		XWebException delete = assertThrows(XWebException.class, () -> this.browser.deleteAllCookies());

		assertFalse(fullText(read).contains(SECRET));
		assertFalse(fullText(delete).contains(SECRET));
	}

	@Test
	public void deleteAllCookiesDelegates() throws Exception
	{
		this.browser.deleteAllCookies();
		Mockito.verify(this.options).deleteAllCookies();
	}
}

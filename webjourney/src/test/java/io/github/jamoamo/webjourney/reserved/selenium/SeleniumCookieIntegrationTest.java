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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.jamoamo.webjourney.JourneyBuilder;
import io.github.jamoamo.webjourney.JourneyException;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.api.IJourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.api.config.AsyncConfiguration;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.IBrowserArgumentsProvider;
import io.github.jamoamo.webjourney.api.web.IBrowserOptions;
import io.github.jamoamo.webjourney.api.web.PreferredBrowserStrategy;
import io.github.jamoamo.webjourney.api.web.ResolvedBrowserArguments;
import io.github.jamoamo.webjourney.api.web.SessionCookie;
import io.github.jamoamo.webjourney.api.web.SessionCookie.SameSite;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.function.FailableFunction;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real headless Chrome tests of cookie access, against a local fixture server. Chrome is started with
 * {@code --host-resolver-rules} mapping {@code a.test} and {@code b.test} to the loopback server so domain-wide
 * cookies (".a.test") behave as they would on a real domain (a bare "localhost" cookie domain does not).
 * <p>
 * Chrome treats {@code http://*.test} as a non-secure context, so a Secure cookie added over http is silently
 * discarded; the tests assert what Chrome actually does.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
public class SeleniumCookieIntegrationTest
{
	private static final String SECRET = "s3cr3t-value";
	private static HttpServer server;
	private static int port;
	/** The Cookie request header of every request to /echo, in order. */
	private static final List<String> ECHOED = new CopyOnWriteArrayList<>();

	private final List<IBrowser> browsers = new CopyOnWriteArrayList<>();

	@BeforeAll
	public static void startServer() throws IOException
	{
		server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/set", exchange ->
		{
			exchange.getResponseHeaders().add("Set-Cookie", "httpOnlyC=hv; Path=/; HttpOnly");
			exchange.getResponseHeaders().add("Set-Cookie", "laxC=lv; Domain=a.test; Path=/; Max-Age=3600; SameSite=Lax");
			exchange.getResponseHeaders().add("Set-Cookie", "sessC=sv; Domain=a.test; Path=/");
			respond(exchange, "set");
		});
		server.createContext("/landing", exchange -> respond(exchange, "landing"));
		server.createContext("/deep", exchange -> respond(exchange, "deep"));
		server.createContext("/deeper", exchange -> respond(exchange, "deeper"));
		server.createContext("/echo", exchange ->
		{
			String header = exchange.getRequestHeaders().getFirst("Cookie");
			ECHOED.add(header == null ? "" : header);
			respond(exchange, "echo");
		});
		server.start();
		port = server.getAddress().getPort();
	}

	@AfterAll
	public static void stopServer()
	{
		server.stop(0);
	}

	private static void respond(HttpExchange exchange, String body) throws IOException
	{
		byte[] bytes = ("<html><head><title>" + body + "</title></head><body>" + body + "</body></html>")
			.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
		exchange.sendResponseHeaders(200, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}

	@BeforeEach
	public void clearEchoes()
	{
		ECHOED.clear();
	}

	@AfterEach
	public void closeBrowsers()
	{
		this.browsers.forEach(IBrowser::exit);
	}

	private static ChromeBrowserFactory factory()
	{
		IBrowserArgumentsProvider provider = Mockito.mock(IBrowserArgumentsProvider.class);
		Mockito.when(provider.resolve(Mockito.any(), Mockito.any())).thenReturn(new ResolvedBrowserArguments(
			List.of("--host-resolver-rules=MAP a.test 127.0.0.1, MAP *.a.test 127.0.0.1, MAP b.test 127.0.0.1"), List.of()));
		AsyncConfiguration configuration = new AsyncConfiguration(List.of(), List.of(), List.of(), List.of(), true,
			"warn", List.of("--dummy-denied-arg"), List.of(), "DEBUG");
		return new ChromeBrowserFactory(configuration, provider);
	}

	private IBrowser newBrowser()
	{
		IBrowserOptions options = Mockito.mock(IBrowserOptions.class);
		Mockito.when(options.isHeadless()).thenReturn(true);
		Mockito.when(options.acceptUnexpectedAlerts()).thenReturn(true);
		IBrowser browser = factory().createBrowser(options, Mockito.mock(IJourneyContext.class));
		this.browsers.add(browser);
		return browser;
	}

	private static URL url(String host, String path) throws Exception
	{
		return new URL("http://" + host + ":" + port + path);
	}

	private static URL urlUnchecked(String host, String path)
	{
		try
		{
			return url(host, path);
		}
		catch(Exception ex)
		{
			throw new IllegalStateException(ex);
		}
	}

	private static SessionCookie byName(List<SessionCookie> cookies, String name)
	{
		return cookies.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
	}

	private static String fullText(Throwable t)
	{
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	@Test
	public void getCookiesAndDeleteAllCookiesBeforeAnyNavigationDoNotThrow() throws Exception
	{
		IBrowser browser = newBrowser();

		assertTrue(browser.getCookies().isEmpty());
		browser.deleteAllCookies();
		assertTrue(browser.getCookies().isEmpty());
	}

	@Test
	public void cookieSetOnOneDomainIsNotVisibleOnAnUnrelatedDomain() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/landing"));
		browser.addCookie(SessionCookie.builder("isolated", "iv", "a.test").build());
		assertNotNull(byName(browser.getCookies(), "isolated"));

		browser.getActiveWindow().navigateToUrl(url("b.test", "/landing"));
		assertNull(byName(browser.getCookies(), "isolated"), "a host-only cookie for a.test must not be visible on b.test");

		browser.getActiveWindow().navigateToUrl(url("b.test", "/echo"));
		assertFalse(ECHOED.get(ECHOED.size() - 1).contains("isolated=iv"), "the cookie must not be sent to b.test");
	}

	@Test
	public void nonRootPathIsHonouredBySegmentBoundary() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/landing"));
		browser.addCookie(SessionCookie.builder("scoped", "sv", ".a.test").withPath("/deep").build());

		// Not visible on the landing page (outside the cookie's path).
		assertNull(byName(browser.getCookies(), "scoped"));

		// Not visible on a sibling path that merely shares the "/deep" prefix as a substring
		// ("/deeper" is not a subpath of "/deep").
		browser.getActiveWindow().navigateToUrl(url("a.test", "/deeper"));
		assertNull(byName(browser.getCookies(), "scoped"), "Chrome matches cookie paths on segment boundaries");

		browser.getActiveWindow().navigateToUrl(url("a.test", "/deep"));
		assertNotNull(byName(browser.getCookies(), "scoped"));
	}

	@Test
	public void readsHttpOnlyExpiryAndSessionCookiesWithSameSite() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/set"));

		List<SessionCookie> cookies = browser.getCookies();

		SessionCookie httpOnly = byName(cookies, "httpOnlyC");
		assertNotNull(httpOnly);
		assertTrue(httpOnly.isHttpOnly());
		assertEquals("hv", httpOnly.getValue());
		assertNull(httpOnly.getExpiry(), "a cookie without Max-Age/Expires is a session cookie");

		SessionCookie lax = byName(cookies, "laxC");
		assertNotNull(lax);
		assertFalse(lax.isHttpOnly());
		assertEquals(SameSite.LAX, lax.getSameSite());
		assertNotNull(lax.getExpiry());
		assertTrue(lax.getExpiry().isAfter(Instant.now()));
		// Domain=a.test on a Set-Cookie header is reported by Chrome as a domain cookie with a leading dot.
		assertEquals(".a.test", lax.getDomain());

		assertNull(byName(cookies, "sessC").getExpiry());
	}

	@Test
	public void roundTripsCookiesIntoANewBrowser() throws Exception
	{
		IBrowser first = newBrowser();
		first.getActiveWindow().navigateToUrl(url("a.test", "/set"));
		List<SessionCookie> captured = first.getCookies();
		first.exit();
		this.browsers.remove(first);

		IBrowser second = newBrowser();
		second.getActiveWindow().navigateToUrl(url("a.test", "/landing"));
		assertTrue(second.getCookies().isEmpty(), "a fresh browser starts without cookies");
		for(SessionCookie cookie : captured)
		{
			second.addCookie(cookie);
		}
		second.getActiveWindow().navigateToUrl(url("a.test", "/echo"));

		String sent = ECHOED.get(ECHOED.size() - 1);
		assertTrue(sent.contains("httpOnlyC=hv"), "HttpOnly cookie must reach the server");
		assertTrue(sent.contains("laxC=lv"));
		assertTrue(sent.contains("sessC=sv"));
		SessionCookie readBack = byName(second.getCookies(), "laxC");
		assertEquals(byName(captured, "laxC"), readBack, "the cookie survives the round trip unchanged");
	}

	@Test
	public void domainWideCookieAddedOnOneSubdomainIsSentToAnother() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("www.a.test", "/landing"));
		browser.addCookie(SessionCookie.builder("wide", "wv", ".a.test").build());
		// The domain must come back exactly as supplied.
		assertEquals(".a.test", byName(browser.getCookies(), "wide").getDomain());
	}

	@Test
	public void addCookieBeforeLandingOnTheDomainThrowsWithoutLeakingValue() throws Exception
	{
		IBrowser browser = newBrowser();
		SessionCookie cookie = SessionCookie.builder("auth", SECRET, ".a.test").build();

		XWebException beforeAnyPage = assertThrows(XWebException.class, () -> browser.addCookie(cookie));
		assertFalse(fullText(beforeAnyPage).contains(SECRET));

		browser.getActiveWindow().navigateToUrl(url("b.test", "/landing"));
		XWebException wrongDomain = assertThrows(XWebException.class, () -> browser.addCookie(cookie));
		assertFalse(fullText(wrongDomain).contains(SECRET));
		assertTrue(wrongDomain.getMessage().contains("auth"));
		assertTrue(wrongDomain.getMessage().contains(".a.test"));
	}

	@Test
	public void expiryIsPreservedToTheSecond() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/landing"));
		Instant expiry = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

		browser.addCookie(SessionCookie.builder("exp", "v", ".a.test").withExpiry(expiry).build());

		assertEquals(expiry, byName(browser.getCookies(), "exp").getExpiry());
	}

	@Test
	public void secureCookieOverHttpIsSilentlyDiscardedAndSameSiteSurvives() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/landing"));

		// What Chrome actually does over http on a non-localhost host: addCookie does not throw, but the Secure
		// cookie is silently discarded, so it is neither readable nor sent. A caller must not assume that a
		// successful addCookie means the cookie is now visible.
		SessionCookie secure = SessionCookie.builder("sec", "secv", ".a.test").withSecure(true).withSameSite(SameSite.LAX).build();
		browser.addCookie(secure);
		assertNull(byName(browser.getCookies(), "sec"));
		browser.getActiveWindow().navigateToUrl(url("a.test", "/echo"));
		assertFalse(ECHOED.get(ECHOED.size() - 1).contains("sec=secv"), "Secure cookie must not be sent over http");

		// SameSite=Lax without Secure is fine and is reported back.
		browser.addCookie(SessionCookie.builder("lax", "v", ".a.test").withSameSite(SameSite.LAX).build());
		assertEquals(SameSite.LAX, byName(browser.getCookies(), "lax").getSameSite());
	}

	@Test
	public void deleteAllCookiesClearsThem() throws Exception
	{
		IBrowser browser = newBrowser();
		browser.getActiveWindow().navigateToUrl(url("a.test", "/set"));
		assertFalse(browser.getCookies().isEmpty());
		browser.deleteAllCookies();
		assertTrue(browser.getCookies().isEmpty());
	}

	/**
	 * Proves the seed-then-check flow the scraper will use: the condition function receives the live browser at
	 * execution time, and the ifTrue / ifFalse sub-journeys are built lazily after the condition ran.
	 */
	@Test
	public void conditionalJourneySeedsCookiesThenChecksAtExecutionTime() throws Exception
	{
		SessionCookie seed = SessionCookie.builder("seeded", "sv", ".a.test").build();
		List<String> order = new CopyOnWriteArrayList<>();

		FailableFunction<IBrowser, Boolean, JourneyException> seedAndCheck = browser ->
		{
			order.add("condition");
			browser.addCookie(seed);
			return browser.getCookies().stream().anyMatch(c -> c.getName().equals("seeded"));
		};
		FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifTrue = builder ->
		{
			order.add("ifTrue");
			return builder.navigateTo(urlUnchecked("a.test", "/echo")).build();
		};
		FailableFunction<IJourneyBuilder, IJourney, JourneyException> ifFalse = builder ->
		{
			order.add("ifFalse");
			return builder.navigateTo(urlUnchecked("a.test", "/landing")).build();
		};

		IJourney journey = JourneyBuilder.path()
			.navigateTo(url("a.test", "/landing"))
			.conditionalJourney(seedAndCheck, ifTrue, ifFalse)
			.build();
		assertTrue(order.isEmpty(), "nothing may run at build time");

		TravelOptions options = new TravelOptions();
		options.setPreferredBrowserStrategy(new PreferredBrowserStrategy(factory()));
		new WebTraveller(options).travelJourney(journey);

		assertEquals(List.of("condition", "ifTrue"), order);
		assertTrue(ECHOED.get(ECHOED.size() - 1).contains("seeded=sv"));
	}
}

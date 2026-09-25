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
package io.github.jamoamo.webjourney.api;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class ConnectionFailuresTest
{
	private static final String CHROMIUM_REFUSAL = "unknown error: net::ERR_CONNECTION_REFUSED";

	@Test
	public void testIsConnectionRefused_connectExceptionSayingRefused_isRefused()
	{
		assertTrue(ConnectionFailures.isConnectionRefused(new ConnectException("Connection refused")));
		assertTrue(ConnectionFailures.isConnectionRefused(new ConnectException("Connection refused: connect")));
	}

	@Test
	public void testIsConnectionRefused_connectExceptionMessageCase_isIgnored()
	{
		assertTrue(ConnectionFailures.isConnectionRefused(new ConnectException("CONNECTION REFUSED")));
	}

	@Test
	public void testIsConnectionRefused_connectTimeout_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new ConnectException("Connection timed out: connect")));
	}

	@Test
	public void testIsConnectionRefused_connectExceptionWithoutMessage_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new ConnectException()));
	}

	@Test
	public void testIsConnectionRefused_connectExceptionDeepInCauseChain_isRefused()
	{
		Throwable failure = new RuntimeException("journey failed",
			new IllegalStateException("action failed",
				new RuntimeException("navigation failed", new ConnectException("Connection refused"))));

		assertTrue(ConnectionFailures.isConnectionRefused(failure));
	}

	@Test
	public void testIsConnectionRefused_webDriverExceptionWithChromiumRefusal_isRefused()
	{
		assertTrue(ConnectionFailures.isConnectionRefused(new WebDriverException(CHROMIUM_REFUSAL)));
	}

	@Test
	public void testIsConnectionRefused_webDriverExceptionWithChromiumRefusalInCause_isRefused()
	{
		assertTrue(ConnectionFailures.isConnectionRefused(
			new RuntimeException("journey failed", new WebDriverException(CHROMIUM_REFUSAL))));
	}

	@Test
	public void testIsConnectionRefused_unhandledAlertWithRefusalText_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new UnhandledAlertException(CHROMIUM_REFUSAL)));
	}

	@Test
	public void testIsConnectionRefused_javascriptExceptionWithRefusalText_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new JavascriptException(CHROMIUM_REFUSAL)));
	}

	@Test
	public void testIsConnectionRefused_refusalTextOnlyInAPageControlledCause_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(
			new RuntimeException("journey failed", new JavascriptException(CHROMIUM_REFUSAL))));
	}

	@Test
	public void testIsConnectionRefused_plainExceptionWithRefusalText_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new RuntimeException(CHROMIUM_REFUSAL)));
	}

	@Test
	public void testIsConnectionRefused_nearMisses_areNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(
			new WebDriverException("unknown error: net::ERR_CONNECTION_RESET")));
		assertFalse(ConnectionFailures.isConnectionRefused(
			new WebDriverException("unknown error: net::ERR_CONNECTION_TIMED_OUT")));
		assertFalse(ConnectionFailures.isConnectionRefused(
			new WebDriverException("unknown error: net::ERR_NAME_NOT_RESOLVED")));
		assertFalse(ConnectionFailures.isConnectionRefused(new SocketTimeoutException("Read timed out")));
		assertFalse(ConnectionFailures.isConnectionRefused(new NoRouteToHostException("No route to host")));
	}

	@Test
	public void testIsConnectionRefused_unrelatedFailure_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(
			new RuntimeException("journey failed", new IllegalStateException("element missing"))));
	}

	@Test
	public void testIsConnectionRefused_nullMessages_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(new RuntimeException(new IllegalStateException())));
	}

	@Test
	public void testIsConnectionRefused_null_isNotRefused()
	{
		assertFalse(ConnectionFailures.isConnectionRefused(null));
	}

	@Test
	public void testIsConnectionRefused_selfReferencingCause_doesNotLoop()
	{
		Throwable selfReferencing = new SelfReferencingException("not a refusal");

		assertSame(selfReferencing, selfReferencing.getCause());
		assertTimeoutPreemptively(Duration.ofSeconds(5),
			() -> assertFalse(ConnectionFailures.isConnectionRefused(selfReferencing)));
	}

	@Test
	public void testIsConnectionRefused_causeCycleBetweenTwoExceptions_doesNotLoop()
	{
		Throwable a = new RuntimeException("a");
		Throwable b = new RuntimeException("b");
		a.initCause(b);
		b.initCause(a);

		assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertFalse(ConnectionFailures.isConnectionRefused(a)));
	}

	@Test
	public void testIsConnectionRefused_causeCycleContainingARefusal_isRefused()
	{
		Throwable a = new RuntimeException("a");
		Throwable refusal = new ConnectException("Connection refused");
		a.initCause(refusal);
		refusal.initCause(a);

		assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertTrue(ConnectionFailures.isConnectionRefused(a)));
	}

	@Test
	public void testIsConnectionRefused_causeCycleContainingAChromiumRefusal_isRefused()
	{
		Throwable a = new RuntimeException("a");
		Throwable refusal = new WebDriverException(CHROMIUM_REFUSAL);
		a.initCause(refusal);
		refusal.initCause(a);

		assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertTrue(ConnectionFailures.isConnectionRefused(a)));
	}

	/**
	 * Throwable.initCause refuses self-causation, so override getCause to model a badly behaved exception.
	 */
	private static final class SelfReferencingException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		SelfReferencingException(String message)
		{
			super(message);
		}

		@Override
		public synchronized Throwable getCause()
		{
			return this;
		}
	}
}

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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;

/**
 * Classifies journey failures by their underlying connection problem, so callers can react to them the same way
 * WebJourney itself does.
 *
 * @author James Amoore
 */
public final class ConnectionFailures
{
	// Chromium's net-error code; it reaches us inside the message of a Selenium WebDriverException.
	private static final String CHROMIUM_CONNECTION_REFUSED = "ERR_CONNECTION_REFUSED";
	private static final String REFUSED = "refused";

	private ConnectionFailures()
	{
	}

	/**
	 * Whether the failure, or any exception in its cause chain, is a refused connection. An exception in the chain is
	 * taken to be a refused connection if it is:
	 * <ul>
	 * <li>a {@link ConnectException} whose message contains "refused" (ignoring case), or</li>
	 * <li>a Selenium {@link WebDriverException} whose message contains the Chromium error code
	 * {@code ERR_CONNECTION_REFUSED}.</li>
	 * </ul>
	 * Other connection problems are not refusals, so are not matched: a connect timeout ("Connection timed out"), an
	 * unreachable host, a reset connection or a name that doesn't resolve. A null failure is not a refused connection,
	 * and a cause chain that loops back on itself is only walked once.
	 * <p>
	 * The message check is deliberately limited to {@code WebDriverException}, and excludes its subclasses
	 * {@link UnhandledAlertException} and {@link JavascriptException}, because their messages can contain text
	 * controlled by the page being visited, which could otherwise be made to look like a refusal.
	 * <p>
	 * This is a heuristic: it depends on how the JVM and the browser word their errors, so what it recognises may change
	 * between releases.
	 * <p>
	 * A refused connection is not something retrying a second later will fix, and repeating it against a host that is
	 * actively refusing us only adds to whatever is causing the refusal (e.g. a rate limit or IP block). That is why
	 * {@link RetryPolicyBuilder} does not retry one by default. The consequence is that a refusal now fails on the
	 * first attempt, with no retry delay, so a caller that runs whole journeys in a loop should not rely on retries to
	 * slow it down: use the listener overload of
	 * {@link IJourneyBuilder#bestEffortJourney(String, org.apache.commons.lang3.function.FailableFunction, IBestEffortOutcomeListener)}
	 * to notice a refusal and back off (or return {@link BestEffortDecision#ABORT}).
	 *
	 * @param failure the failure to inspect, including its causes
	 * @return true if the failure, or any of its causes, is a refused connection
	 */
	public static boolean isConnectionRefused(Throwable failure)
	{
		for(Throwable current : ExceptionUtils.getThrowableList(failure))
		{
			if(isRefusedConnectException(current) || isChromiumRefusal(current))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isRefusedConnectException(Throwable failure)
	{
		return failure instanceof ConnectException && StringUtils.containsIgnoreCase(failure.getMessage(), REFUSED);
	}

	private static boolean isChromiumRefusal(Throwable failure)
	{
		return failure instanceof WebDriverException
			&& !(failure instanceof UnhandledAlertException)
			&& !(failure instanceof JavascriptException)
			&& StringUtils.contains(failure.getMessage(), CHROMIUM_CONNECTION_REFUSED);
	}
}

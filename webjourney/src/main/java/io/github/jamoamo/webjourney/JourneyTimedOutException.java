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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import java.util.concurrent.TimeUnit;

/**
 * An exception that occurs when a journey execution times out.
 * 
 * <p>
 * This exception is thrown when an asynchronous journey execution exceeds
 * the specified timeout duration. It extends {@link JourneyException} and
 * provides additional context about the timeout parameters.
 * </p>
 * 
 * @author James Amoore
 * @see JourneyException
 * @see io.github.jamoamo.webjourney.api.IAsyncJourneyExecutor
 * @since 1.0.0
 */
@SuppressWarnings("MutableException")
public class JourneyTimedOutException extends JourneyException
{
	private static final String MESSAGE = "Journey execution timed out after %d %s";
	
	private final long timeout;
	private final TimeUnit timeUnit;
	
	/**
	 * Constructor with timeout information.
	 * 
	 * @param message The timeout error message
	 * @param timeout The timeout duration that was exceeded
	 * @param timeUnit The time unit of the timeout
	 */
	public JourneyTimedOutException(String message, long timeout, TimeUnit timeUnit)
	{
		super(message);
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}
	
	/**
	 * Constructor with timeout information and cause.
	 * 
	 * @param timeout The timeout duration that was exceeded
	 * @param timeUnit The time unit of the timeout
	 * @param cause The underlying cause of the timeout
	 */
	public JourneyTimedOutException(long timeout, TimeUnit timeUnit, Throwable cause)
	{
		super(String.format(MESSAGE, timeout, timeUnit), cause);
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}
	
	/**
	 * Constructor with timeout information and breadcrumb.
	 * 
	 * @param timeout The timeout duration that was exceeded
	 * @param timeUnit The time unit of the timeout
	 * @param breadcrumb The journey breadcrumb at the time of timeout
	 */
	public JourneyTimedOutException(long timeout, TimeUnit timeUnit, IJourneyBreadcrumb breadcrumb)
	{
		super(String.format(MESSAGE, timeout, timeUnit), breadcrumb);
		this.timeout = timeout;
		this.timeUnit = timeUnit;
	}
	
	/**
	 * Gets the timeout duration that was exceeded.
	 * 
	 * @return The timeout duration
	 */
	public long getTimeout()
	{
		return this.timeout;
	}
	
	/**
	 * Gets the time unit of the timeout.
	 * 
	 * @return The time unit
	 */
	public TimeUnit getTimeUnit()
	{
		return this.timeUnit;
	}
	
	/**
	 * Gets a formatted string representation of the timeout.
	 * 
	 * @return A string like "30 SECONDS" or "5 MINUTES"
	 */
	public String getFormattedTimeout()
	{
		return this.timeout + " " + this.timeUnit;
	}
}

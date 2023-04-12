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
package com.github.jamoamo.webjourney.reserved.log;

/**
 * The message to be logged.
 * @author James Amoore
 */
public final class LogMessage
{
	private final LogSeverity severity;
	private final String message;
	
	/**
	 * Create a new instance.
	 * @param severity The severity of the logged message.
	 * @param message The message to log.
	 */
	public LogMessage(LogSeverity severity, String message)
	{
		this.severity = severity;
		this.message = message;
	}
	
	/**
	 * Get the log severity.
	 * @return the log severity.
	 */
	public LogSeverity getSeverity()
	{
		return this.severity;
	}
	
	/**
	 * Get the log message.
	 * @return the log message.
	 */
	public String getMessage()
	{
		return this.message;
	}
}

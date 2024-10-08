/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package io.github.jamoamo.webjourney.api.mapper;

/**
 * An exception that occurs whilst mapping a value from the HTML document to the target field.
 *
 * @author James Amoore
 */
public class XValueMappingException
		  extends Exception
{
	private static final String MAPPING_ERROR = "Mapping error.";

	/**
	 * Creates a new instance with the Exception that caused the mapping error.
	 *
	 * @param cause The cause of the exception.
	 */
	public XValueMappingException(Exception cause)
	{
		this("", cause);
	}

	/**
	 * Creates a new instance with a message and the Exception that caused the mapping error.
	 *
	 * @param message The message for the exception.
	 * @param ex      The cause of the exception.
	 */
	public XValueMappingException(String message, Exception ex)
	{
		super(MAPPING_ERROR + message, ex);
	}

	/**
	 * Creates a new instance with a message.
	 *
	 * @param message The message for the exception.
	 */
	public XValueMappingException(String message)
	{
		super(MAPPING_ERROR + message);
	}

}

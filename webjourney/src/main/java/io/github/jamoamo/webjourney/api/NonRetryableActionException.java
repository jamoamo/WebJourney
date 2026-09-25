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

/**
 * Thrown from an action's {@code executeActionImpl} to fail the action without it being retried, whatever
 * {@link IRetryPolicy} is in force. The cause is the failure the action should fail with; {@link AWebAction} unwraps it
 * and rethrows it once the retry policy has returned, so callers never see this type.
 *
 * @author James Amoore
 */
public final class NonRetryableActionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates the exception.
	 *
	 * @param failure the failure the action should fail with, must not be null.
	 */
	public NonRetryableActionException(Exception failure)
	{
		super(failure);
		if(failure == null)
		{
			throw new IllegalArgumentException("failure cannot be null");
		}
	}
}

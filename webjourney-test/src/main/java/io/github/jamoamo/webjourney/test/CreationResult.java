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
package io.github.jamoamo.webjourney.test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Result of entity creation.
 * @author James Amoore
 * @param <T> The type of the created entity
 */
public final class CreationResult<T>
{
	/**
	 * The creation result.
	 */
	public enum Result
	{
		/**
		 * If the creation was successful.
		 */
		SUCCESS,
		/**
		 * if the entity creation failed.
		 */
		FAILED
	}
	
	private Result result;
	private Exception failureException;
	private T createdEntiy;
	
	protected CreationResult()
	{
		
	}

	/**
	 * @return the creation result
	 */
	public Result getResult()
	{
		return this.result;
	}

	protected void setResult(Result result)
	{
		this.result = result;
	}

	/**
	 * @return the exception that caused the creation to fail
	 */
	@SuppressFBWarnings(value = "EI_EXPOSE_REP")
	public Exception getFailureException()
	{
		return this.failureException;
	}

	protected void setFailureException(Exception failureException)
	{
		this.failureException = failureException;
	}

	/**
	 * @return the entity that was created
	 */
	public T getCreatedEntiy()
	{
		return this.createdEntiy;
	}

	protected void setCreatedEntiy(T createdEntiy)
	{
		this.createdEntiy = createdEntiy;
	}
}

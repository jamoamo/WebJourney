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

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class BestEffortOutcomeTest
{
	@Test
	public void testSuccess_isSuccessWithNoFailureAndCarriesTheDetails()
	{
		BestEffortOutcome outcome = BestEffortOutcome.success("login", Duration.ofMillis(250), 2);

		assertTrue(outcome.isSuccess());
		assertNull(outcome.getFailure());
		assertEquals("login", outcome.getName());
		assertEquals(Duration.ofMillis(250), outcome.getElapsed());
		assertEquals(2, outcome.getRetryCount());
	}

	@Test
	public void testFailure_isNotSuccessAndKeepsTheExceptionAndDetails()
	{
		IllegalStateException ex = new IllegalStateException("broken");

		BestEffortOutcome outcome = BestEffortOutcome.failure("login", ex, Duration.ofSeconds(1), 3);

		assertFalse(outcome.isSuccess());
		assertSame(ex, outcome.getFailure());
		assertEquals("login", outcome.getName());
		assertEquals(Duration.ofSeconds(1), outcome.getElapsed());
		assertEquals(3, outcome.getRetryCount());
	}

	@Test
	public void testSuccess_eachCallIsItsOwnOutcome()
	{
		assertNotSame(BestEffortOutcome.success("a", Duration.ZERO, 0), BestEffortOutcome.success("a", Duration.ZERO, 0));
	}

	@Test
	public void testFailure_null_isRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.failure("login", null, Duration.ZERO, 0));
	}

	@Test
	public void testFactories_invalidArguments_areRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.success(null, Duration.ZERO, 0));
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.success(" ", Duration.ZERO, 0));
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.success("login", null, 0));
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.success("login", Duration.ofMillis(-1), 0));
		assertThrows(IllegalArgumentException.class, () -> BestEffortOutcome.success("login", Duration.ZERO, -1));
	}

	@Test
	public void testToString_describesTheOutcome()
	{
		assertEquals("BestEffortOutcome[name=login, success, elapsed=250ms, retries=2]",
			BestEffortOutcome.success("login", Duration.ofMillis(250), 2).toString());
		assertTrue(BestEffortOutcome.failure("login", new IllegalStateException("broken"), Duration.ZERO, 0)
			.toString().contains("failure=java.lang.IllegalStateException: broken"));
	}
}

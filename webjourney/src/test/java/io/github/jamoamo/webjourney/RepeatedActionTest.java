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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.SubJourney;
import io.github.jamoamo.webjourney.RepeatedAction;
import io.github.jamoamo.webjourney.IJourneyContext;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class RepeatedActionTest
{
	
	public RepeatedActionTest()
	{
	}

	/**
	 * Test of executeAction method, of class RepeatedAction.
	 */
	@Test
	public void testConstructor_nullrepeater()
	{
		RuntimeException assertThrows =
				  assertThrows(RuntimeException.class, () -> new RepeatedAction<>(null, new SubJourney(new ArrayList<>())));
		assertEquals("repeater cannot be null", assertThrows.getMessage());
	}
	
	/**
	 * Test of executeAction method, of class RepeatedAction.
	 */
	@Test
	public void testConstructor_nullsubjourney()
	{
		RuntimeException assertThrows =
				  assertThrows(RuntimeException.class, () -> new RepeatedAction<>(context -> new ArrayList<Integer>(0), null));
		assertEquals("Sub Journey cannot be null", assertThrows.getMessage());
	}
	
	public void testExecuteAction()
	{
		RepeatedAction<Integer> action =new RepeatedAction<>(context -> Arrays.asList(new Integer[]{1,2,3,4,5}), null);
		IJourneyContext context = Mockito.mock(IJourneyContext.class);
		action.executeAction(context);
	}
}

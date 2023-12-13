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
package com.github.jamoamo.webjourney.reserved.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class CombinedTransformerTest
{
	
	public CombinedTransformerTest()
	{
	}

	/**
	 * Test of transformValue method, of class CombinedTransformer.
	 */
	@Test
	public void testTransformValue()
	{
		ITransformer<String> indivTransformer = Mockito.mock(ITransformer.class);
		Mockito.when(indivTransformer.transformValue("Test1")).thenReturn("!Test!1!");
		
		ITransformer<String> indivTransformer2 = Mockito.mock(ITransformer.class);
		Mockito.when(indivTransformer2.transformValue("!Test!1!")).thenReturn("<!Test!1!>");
		
		CombinedTransformer transformer = new CombinedTransformer(indivTransformer, indivTransformer2);
		
		String transformValue = transformer.transformValue("Test1");
		
		assertEquals("<!Test!1!>", transformValue);
	}
	
}

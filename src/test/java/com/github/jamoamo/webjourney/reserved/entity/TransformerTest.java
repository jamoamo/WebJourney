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

import com.github.jamoamo.webjourney.annotation.Transformation;
import com.github.jamoamo.webjourney.api.transform.ATransformationFunction;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class TransformerTest
{
	
	public static class Function extends ATransformationFunction
	{
		@Override
		public String transform(String extractedValue, String[] parameters)
		{
			return extractedValue + "!";
		}
	}

	/**
	 * Test of transformValue method, of class Transformer.
	 */
	@Test
	public void testTransformValue()
	{
		Transformation transformation = new Transformation()
		{
			@Override
			public Class<? extends ATransformationFunction> transformFunction()
			{
				return Function.class;
			}

			@Override
			public String[] parameters()
			{
				return new String[]{};
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return Transformation.class;
			}
		};
		
		Transformer transformer = new Transformer(transformation);
		String transformValue = transformer.transformValue("Value");
		assertEquals("Value!", transformValue);
	}
	
}

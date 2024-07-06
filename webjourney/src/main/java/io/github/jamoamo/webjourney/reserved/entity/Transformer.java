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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.annotation.Transformation;
import io.github.jamoamo.webjourney.api.transform.ATransformationFunction;
import io.github.jamoamo.webjourney.reserved.reflection.InstanceCreator;

/**
 *
 * @author James Amoore
 */
class Transformer implements ITransformer<String>
{
	private final Transformation transformation;
	
	Transformer(Transformation transformation)
	{
		this.transformation = transformation;
	}

	@Override
	public String transformValue(String value)
	{
		ATransformationFunction function =
				  InstanceCreator.getInstance().createInstance(this.transformation.transformFunction());
		return function.transform(value, this.transformation.parameters());
	}
}

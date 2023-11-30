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

import com.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import com.github.jamoamo.webjourney.reserved.reflection.TypeInfo;

/**
 *
 * @author James Amoore
 */
final class Transformers
{
	private Transformers()
	{
	}

	public static ITransformer getTransformerForField(EntityFieldDefn defn)
	{
		EntityAnnotations annotations = defn.getAnnotations();
		TypeInfo typeInfo = TypeInfo.forClass(defn.getFieldType());
		if(annotations.hasTransformation())
		{
			return getTransformerForTransformation(annotations, typeInfo, defn);
		}
		if(typeInfo.isCollectionType() & !defn.getAnnotations().hasMappedCollection() && annotations.
			hasRegexExtractValue())
		{
			return new CollectionTransformer(new RegexTransformation(annotations.
						getRegexExtract()));
		}
		else if(annotations.hasRegexExtractValue())
		{
			return new RegexTransformation(annotations.getRegexExtract());
		}

		return null;
	}

	private static ITransformer getTransformerForTransformation(EntityAnnotations annotations, TypeInfo typeInfo,
																					EntityFieldDefn defn)
	{
		if(annotations.hasRegexExtractValue())
		{
			return new CombinedTransformer(new RegexTransformation(annotations.getRegexExtract()), new Transformer(
				annotations.getTransformation()));
		}
		if(typeInfo.isCollectionType() & !defn.getAnnotations().hasMappedCollection())
		{
			if(annotations.hasRegexExtractValue())
			{
				return new CollectionTransformer(new CombinedTransformer(new RegexTransformation(annotations.
					getRegexExtract()), new Transformer(annotations.getTransformation())));
			}
			return new CollectionTransformer(new Transformer(annotations.getTransformation()));
		}
		else
		{
			return new Transformer(defn.getAnnotations().getTransformation());
		}
	}
}

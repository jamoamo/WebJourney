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

import io.github.jamoamo.webjourney.reserved.annotation.EntityAnnotations;
import io.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import io.github.jamoamo.webjourney.reserved.regex.RegexGroup;
import io.github.jamoamo.webjourney.reserved.regex.XRegexException;

/**
 *
 * @author James Amoore
 */
final class Transformers
{
	private Transformers()
	{
	}

	public static ITransformer getTransformerForField(EntityFieldDefn defn) throws XEntityFieldDefinitionException
	{
		try
		{
			EntityAnnotations annotations = defn.getAnnotations();
			TypeInfo typeInfo = TypeInfo.forClass(defn.getFieldType());
			if(annotations.hasTransformation())
			{
				return getTransformerForTransformation(annotations, typeInfo, defn);
			}
			if(typeInfo.isCollectionType() & !defn.getAnnotations().hasMappedCollection() && annotations.hasRegexExtract())
			{
				return new CollectionTransformer(new RegexTransformation(annotations.getRegexExtractGroup()));
			}
			else if(annotations.hasRegexExtract())
			{
				return new RegexTransformation(annotations.getRegexExtractGroup());
			}
		}
		catch(XRegexException ex)
		{
			throw new XEntityFieldDefinitionException(ex);
		}

		return null;
	}

	private static ITransformer getTransformerForTransformation(EntityAnnotations annotations, TypeInfo typeInfo,
		EntityFieldDefn defn) throws XRegexException
	{
		if(typeInfo.isCollectionType() & !defn.getAnnotations().hasMappedCollection())
		{
			if(annotations.hasRegexExtract())
			{
				RegexGroup regexGroup = annotations.getRegexExtractGroup();
				return new CollectionTransformer(new CombinedTransformer(new RegexTransformation(regexGroup), 
					new Transformer(annotations.getTransformation())));
			}
			return new CollectionTransformer(new Transformer(annotations.getTransformation()));
		}
		else if(annotations.hasRegexExtract())
		{
			RegexGroup regexGroup = annotations.getRegexExtractGroup();
			return new CombinedTransformer(new RegexTransformation(regexGroup), new Transformer(
				annotations.getTransformation()));
		}
		else
		{
			return new Transformer(defn.getAnnotations().getTransformation());
		}
	}
}

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
import com.github.jamoamo.webjourney.reserved.regex.RegexGroup;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class TransformersTest
{
	
	public TransformersTest()
	{
	}

	/**
	 * Test of getTransformerForField method, of class Transformers.
	 */
	@Test
	public void testGetTransformerForField_transformation_string_regexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> String.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(CombinedTransformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_transformation_collection_notMapped_RegexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(CollectionTransformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_transformation_collection_notMapped_noRegexValue() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(CollectionTransformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_transfomation_collection_Mapped_RegexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.TRUE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(CombinedTransformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_transfomation_collection_Mapped_noRegexValue() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.TRUE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(Transformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_transfomation_string_noRegexValue() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> String.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(Transformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_noTransfomation_collection_notMapped_RegexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(CollectionTransformer.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_noTransfomation_collection_Mapped_RegexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.TRUE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> List.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(RegexTransformation.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_noTransfomation_string_RegexValue() throws Exception
	{
		RegexGroup regex = Mockito.mock(RegexGroup.class);
		
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.TRUE);
		Mockito.when(annotations.getRegexExtractGroup()).thenReturn(regex);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> String.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertInstanceOf(RegexTransformation.class, transformerForField);
	}
	
	@Test
	public void testGetTransformerForField_noTransfomation_string_noRegexValue() throws Exception
	{
		EntityAnnotations annotations = Mockito.mock(EntityAnnotations.class);
		Mockito.when(annotations.hasTransformation()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasRegexExtract()).thenReturn(Boolean.FALSE);
		Mockito.when(annotations.hasMappedCollection()).thenReturn(Boolean.FALSE);
		
		EntityFieldDefn defn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(defn.getAnnotations()).thenReturn(annotations);
		Mockito.when(defn.getFieldType()).then(im -> String.class);
		
		ITransformer transformerForField = Transformers.getTransformerForField(defn);
		assertNull(transformerForField);
	}
}

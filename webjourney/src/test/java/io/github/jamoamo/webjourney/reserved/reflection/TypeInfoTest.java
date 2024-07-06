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
package io.github.jamoamo.webjourney.reserved.reflection;

import io.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class TypeInfoTest
{
	/**
	 * Test of getType method, of class TypeInfo.
	 */
	@Test
	public void testGetType()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertEquals(String.class, info.getType());
	}

	/**
	 * Test of implementsInterface method, of class TypeInfo.
	 */
	@Test
	public void testImplementsInterface_false()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.implementsInterface(Collection.class));
	}
	
	@Test
	public void testImplementsInterface_true()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertTrue(info.implementsInterface(List.class));
	}
	@Test
	public void testImplementsInterface_multiLevel()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertTrue(info.implementsInterface(Collection.class));
	}
	

	/**
	 * Test of hasNoArgsConstructor method, of class TypeInfo.
	 */
	@Test
	public void testHasNoArgsConstructor()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertTrue(info.hasNoArgsConstructor());
	}

	/**
	 * Test of isCollectionType method, of class TypeInfo.
	 */
	@Test
	public void testIsCollectionType_ArrayList()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertTrue(info.isCollectionType());
	}
	
	@Test
	public void testIsCollectionType_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.isCollectionType());
	}

	/**
	 * Test of isStandardType method, of class TypeInfo.
	 */
	@Test
	public void testIsStandardType_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_Integer()
	{
		TypeInfo info = TypeInfo.forClass(Integer.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_int()
	{
		TypeInfo info = TypeInfo.forClass(int.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_Boolean()
	{
		TypeInfo info = TypeInfo.forClass(Boolean.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_boolean()
	{
		TypeInfo info = TypeInfo.forClass(boolean.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_Double()
	{
		TypeInfo info = TypeInfo.forClass(Double.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_double()
	{
		TypeInfo info = TypeInfo.forClass(double.class);
		assertTrue(info.isStandardType());
	}
		
	@Test
	public void testIsStandardType_Float()
	{
		TypeInfo info = TypeInfo.forClass(Float.class);
		assertTrue(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_float()
	{
		TypeInfo info = TypeInfo.forClass(float.class);
		assertTrue(info.isStandardType());
	}
	
	
	@Test
	public void testIsStandardType_ArrayList()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertFalse(info.isStandardType());
	}
	
	@Test
	public void testIsStandardType_LocalDate()
	{
		TypeInfo info = TypeInfo.forClass(LocalDate.class);
		assertTrue(info.isStandardType());
	}

	/**
	 * Test of isStringType method, of class TypeInfo.
	 */
	@Test
	public void testIsStringType_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertTrue(info.isStringType());
	}
	
	@Test
	public void testIsStringType_Integer()
	{
		TypeInfo info = TypeInfo.forClass(Integer.class);
		assertFalse(info.isStringType());
	}
	
	@Test
	public void testIsStringType_ArrayList()
	{
		TypeInfo info = TypeInfo.forClass(ArrayList.class);
		assertFalse(info.isStringType());
	}

	/**
	 * Test of isInteger method, of class TypeInfo.
	 */
	@Test
	public void testIsInteger_Integer()
	{
		TypeInfo info = TypeInfo.forClass(Integer.class);
		assertTrue(info.isInteger());
	}
	
	@Test
	public void testIsInteger_int()
	{
		TypeInfo info = TypeInfo.forClass(int.class);
		assertTrue(info.isInteger());
	}
	
	@Test
	public void testIsInteger_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.isInteger());
	}

	/**
	 * Test of isDouble method, of class TypeInfo.
	 */
	@Test
	public void testIsDouble_Double()
	{
		TypeInfo info = TypeInfo.forClass(Double.class);
		assertTrue(info.isDouble());
	}
	
	@Test
	public void testIsDouble_double()
	{
		TypeInfo info = TypeInfo.forClass(double.class);
		assertTrue(info.isDouble());
	}
	
	@Test
	public void testIsDouble_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.isDouble());
	}

	/**
	 * Test of isArrayType method, of class TypeInfo.
	 */
	@Test
	public void testIsArrayType_StringArray()
	{
		TypeInfo info = TypeInfo.forClass(String[].class);
		assertTrue(info.isArrayType());
	}
	@Test
	public void testIsArrayType_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.isArrayType());
	}

	/**
	 * Test of isDateType method, of class TypeInfo.
	 */
	@Test
	public void testIsDateType_LocalDate()
	{
		TypeInfo info = TypeInfo.forClass(LocalDate.class);
		assertTrue(info.isDateType());
	}
	
	@Test
	public void testIsDateType_String()
	{
		TypeInfo info = TypeInfo.forClass(String.class);
		assertFalse(info.isDateType());
	}
}

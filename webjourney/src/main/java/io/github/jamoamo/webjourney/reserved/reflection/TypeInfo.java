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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author James Amoore
 */
public final class TypeInfo
{
	private final Class<?> theClass;
	
	private TypeInfo(Class<?> theClass)
	{
		this.theClass = theClass;
	}
	
	/**
	 * Creates a TypeInfo.
	 * @param theClass the class to create it for.
	 * @return the TypeInfo.
	 */
	public static TypeInfo forClass(Class<?> theClass)
	{
		return new TypeInfo(theClass);
	}
	
	/**
	 * Return the type.
	 * @return the class type
	 */
	public Class<?> getType()
	{
		return this.theClass;
	}
	
	/**
	 * Determines if the type implements an interface.
	 * @param interfaceClazz the interface class to be tested.
	 * @return boolean indicating if the interface is implemented by the type.
	 */
	public boolean implementsInterface(Class<?> interfaceClazz)
	{
		return getAllExtendedOrImplementedInterfacesRecursively(this.theClass)
			 .contains(interfaceClazz);
	}
	
	/**
	 * Returns @code{true} if the class has a no-args constructor.
	 * @return a boolean indicating if the class has a no-args constructor.
	 */
	public boolean hasNoArgsConstructor()
	{
		try
		{
			this.theClass.getConstructor(new Class[]{});
			return true;
		}
		catch(NoSuchMethodException |
			 SecurityException ex)
		{
			return false;
		}
	}
	
	/**
	 * Determines if the class can be classed a collection. Returns true if the class. 
	 * implements a Collection interface or is an array type.
	 * @return true if the class is a collection.
	 */
	public boolean isCollectionType()
	{
		return implementsInterface(Collection.class) || isArrayType();
	}
	
	/**
	 * Determines if the class is a standard type. Standard type is one of the following:
	 * <ul>
	 *		<li> String.class
	 *		<li> Integer.class (or int.class)
	 *		<li> Double.class (or double.class)
	 *		<li> FLoat.class (or float.class)
	 *		<li> Boolean.class (or boolean.class)
	 * </ul>
	 * @return true if the class is a standard type
	 */
	public boolean isStandardType()
	{
		return isStringType() || isNumericType() || isBoolean() || isDateType();
	}
	
	/**
	 * Determines if the class is Boolean.class or boolean.class.
	 * @return true if the class is a boolean
	 */
	public boolean isBoolean()
	{
		return this.theClass.equals(Boolean.class) || this.theClass.equals(boolean.class);
	}
	
	/**
	 * Determines if the class is String.class.
	 * @return true if the class is String.class
	 */
	public boolean isStringType()
	{
		return this.theClass.equals(String.class);
	}
	
	private boolean isNumericType()
	{
		return this.isInteger() || this.isDouble() || this.isFloat();
	}
	
	/**
	 * Determines if the class is Integer.class or int.class.
	 * @return true if the class is an integer
	 */
	public boolean isInteger()
	{
		return this.theClass.equals(Integer.class) || this.theClass.equals(int.class);
	}
	
	/**
	 * Determines if the class is Double.class or double.class.
	 * @return true if the class is an integer
	 */
	public boolean isDouble()
	{
		return this.theClass.equals(Double.class) || this.theClass.equals(double.class);
	}
	
	private boolean isFloat()
	{
		return this.theClass.equals(Float.class) || this.theClass.equals(float.class);
	}
	
	/**
	 * Determines if the class is an array.
	 * @return true if the class is an integer
	 */
	public boolean isArrayType()
	{
		return this.theClass.isArray();
	}
	
	/**
	 * Determines if the class is a date type.
	 * @return true is its a date type
	 */
	public boolean isDateType()
	{
		return this.theClass == LocalDate.class;
	}
	
	/**
	 * Indicates if the type is a primitive type.
	 * @return true if the type is primitive.
	 */
	public boolean isPrimitive()
	{
		return this.theClass.isPrimitive();
	}
	
	private static Set<Class<?>> getAllExtendedOrImplementedInterfacesRecursively(Class<?> clazz) 
	{
		Set<Class<?>> res = new HashSet<>();
		Class<?>[] interfaces = clazz.getInterfaces();
		res.add(clazz);
		if (interfaces.length > 0) 
		{
			 res.addAll(Arrays.asList(interfaces));

			 for (Class<?> interfaze : interfaces)
			 {
				  res.addAll(getAllExtendedOrImplementedInterfacesRecursively(interfaze));
			 }
		}

		return res;
	}
}

/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package com.github.jamoamo.webjourney.reserved.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for creating instances of classes using reflection.
 * 
 * <b>There is no gaurantee that backwards compatibility will be maintained 
 * for classes in reserved packages</b>
 * @author James Amoore
 */
public final class InstanceCreator
{
	public final static InstanceCreator INSTANCE = new InstanceCreator();
	
	/**
	 * @return a new InstanceCreator instance.
	 */
	public static InstanceCreator getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * Creates a new instance of the provided class.
	 * @param <T> The instance type
	 * @param instanceClass The instance type
 	 * @return newly created instance.
	 */
	public <T> T createInstance(Class<T> instanceClass)
	{
		try
		{
			Constructor<T> defaultConstructor = instanceClass.getConstructor(new Class<?>[]{});
			T entity = defaultConstructor.newInstance(new Object[]{});
			return entity;
		}
		catch(NoSuchMethodException ex)
		{
			throw new RuntimeException(
				 String.format("Type does not have a no-args constructor: %s",
					  instanceClass.getSimpleName()),
				 ex);
		}
		catch(InstantiationException
			 | IllegalAccessException
			 | InvocationTargetException ex)
		{
			throw new RuntimeException(
				 String.format("Failed to create an instance of type: %s",
					  instanceClass.getSimpleName()),
				 ex);
		}
	}
}

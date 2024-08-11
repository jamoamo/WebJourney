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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class InstanceCreatorTest
{
	 public static class Test1
	 {

	 }

	 public static class Test2
	 {
		  public Test2()
		  {

		  }

	 }

	 public static class Test3
	 {
		  public Test3(String test3)
		  {

		  }

	 }

	 /**
	  * Test of createInstance method, of class InstanceCreator.
	  */
	 @Test
	 public void testCreateInstance_defaultConstructor()
	 {
		  Test1 instance = InstanceCreator.getInstance()
				.createInstance(Test1.class);
		  assertNotNull(instance);
	 }

	 @Test
	 public void testCreateInstance_emptyConstructor()
	 {
		  Test2 instance = InstanceCreator.getInstance()
				.createInstance(Test2.class);
		  assertNotNull(instance);
	 }

	 @Test
	 public void testCreateInstance_noEmptyConstructor()
	 {
		  InstanceCreator creator = InstanceCreator.getInstance();
		  RuntimeException assertThrows =
				assertThrows(RuntimeException.class, () -> creator.createInstance(Test3.class));
		  assertEquals("Type does not have a no-args constructor: Test3", assertThrows.getMessage());

	 }

}

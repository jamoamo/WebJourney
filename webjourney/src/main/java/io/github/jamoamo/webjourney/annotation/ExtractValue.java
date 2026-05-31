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
package io.github.jamoamo.webjourney.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field.
 * @author James Amoore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtractValue
{
	/**
	 * XPath to the element containing the value to retrieve. XPath should not result in an attribute, only elements.
	 * @return the XPath to the element to be extracted.
	 */
	String path();
	
	/**
	 * The name of the attribute whose value should be extracted. 
	 * @return The attribute name to extract the value of.
	 */
	String attribute() default "";
	
	/**
	 * Is the value extract optional? If true the extraction will result in null if the entity the value is
	 * extracted from doesn't exist.
	 * @return true if the value is optional.
	 */
	boolean optional() default false;

	/**
	 * The maximum number of seconds to wait for the element to be present before reading it.
	 * <p>
	 * When the element is not immediately present the extraction will wait, up to this many seconds, for it to
	 * appear in the page before reading its value.
	 * <ul>
	 * <li>A negative value (the default) means the wait configured on the journey's
	 * {@link io.github.jamoamo.webjourney.api.ITravelOptions#getElementWaitTimeout() travel options} is used.</li>
	 * <li>A value of {@code 0} disables waiting, reading the element immediately.</li>
	 * <li>A positive value waits up to that many seconds for the element to be present.</li>
	 * </ul>
	 * @return the number of seconds to wait for the element, or a negative value to inherit the global default.
	 */
	long waitSeconds() default -1;
}

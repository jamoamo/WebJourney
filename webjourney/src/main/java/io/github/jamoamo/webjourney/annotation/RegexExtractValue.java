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
 * Use a regex expression to extract specific information.
 * @author James Amoore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegexExtractValue
{
	/**
	 * How to Extract the raw value to apply the regex to. Exactly one of {@link #extractValue()} or
	 * {@link #extractTextValue()} must be set - leave this at its default (a blank path) when using
	 * {@link #extractTextValue()} instead.
	 * @return the Extract Value
	 */
	ExtractValue extractValue() default @ExtractValue(path = "");

	/**
	 * How to extract the raw text-node value to apply the regex to, as an alternative to
	 * {@link #extractValue()} for text nodes that cannot be reached by an element-resolving XPath (see
	 * {@link ExtractTextValue}). Exactly one of {@link #extractValue()} or {@link #extractTextValue()} must be
	 * set - leave this at its default (a blank path) when using {@link #extractValue()} instead.
	 * @return the ExtractTextValue to use to get the raw value.
	 */
	ExtractTextValue extractTextValue() default @ExtractTextValue(path = "");

	/**
	 * The regexes to evaluate use to extract the value. The first regex with a matching group will be used.
	 * @return the regexes to use to extract the value.
	 */
	String[] regexes();
	
	/**
	 * 
	 * @return the name of the regex group to extract
	 */
	String groupName();
	
	/**
	 * The default value if the group is not matched. If not specified, empty string will be used as the default.
	 * @return the default value.
	 */
	String defaultValue() default "";
}

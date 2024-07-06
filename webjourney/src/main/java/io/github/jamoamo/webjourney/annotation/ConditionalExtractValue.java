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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set of Conditions extract from url extractors. 
 * @author James Amoore
 */
public @interface ConditionalExtractValue
{
	/**
	* Container for RegexMatch.
	*/
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface RegexMatches
	{
		/**
		 * Container for RegexMatch.
		 * @return set of RegexMatch
		 */
		RegexMatch[] value();
	}
	
	/**
	 * Extract if the regex pattern matches.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@Repeatable(RegexMatches.class)
	public @interface RegexMatch
	{
		/**
		 * @return The extractor to use to get the value to match against the regex pattern.
		 */
		ExtractValue ifExtractValue();
		
		/**
		 * @return The extractor to use to get the value if the regex pattern matches.
		 */
		ExtractValue thenExtractValue();
		
		/**
		 * @return the regex pattern
		 */
		String regexPattern();
	}
}

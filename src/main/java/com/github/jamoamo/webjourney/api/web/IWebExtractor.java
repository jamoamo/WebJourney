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
package com.github.jamoamo.webjourney.api.web;

import java.util.List;
import java.util.function.Function;

/**
 * Web extractor interface.
 *
 * @author James Amoore
 */
public interface IWebExtractor
{
	/**
	 * Extracts an attribute value from an element.
	 * @param <T> The type of the value
	 * @param elementXPath The xpath to the element.
	 * @param attribute The attribute to get the value of.
	 * @param mapFunction The mapping function to map an entity from an attribute value
	 * @return The attribute value.
	 */
	<T> T extractAttribute(String elementXPath, String attribute, Function<String, T> mapFunction);
	
	/**
	 * extracts an entity.
	 *
	 * @param <T>             The type of the entity.
	 * @param xPath           The xpath.
	 * @param mappingFunction the mapping function to map an entity from an element.
	 *
	 * @return the entity instance
	 */
	<T> T extractEntity(String xPath, Function<AElement, T> mappingFunction);

	/**
	 * extracts a list of entities.
	 *
	 * @param <T>             The type of the entity.
	 * @param xPath           The xpath.
	 * @param mappingFunction the mapping function to map an entity from an element.
	 *
	 * @return a list of entity instances
	 */
	<T> List<T> extractEntities(String xPath, Function<AElement, T> mappingFunction);

	/**
	 * extracts a String value from an element.
	 *
	 * @param xPath The xpath.
	 *
	 * @return a string value from the element.
	 */
	String extractValue(String xPath);

	/**
	 * extracts a value from the element text.
	 *
	 * @param <T>             The type of the value.
	 * @param xPath           The xpath.
	 * @param mappingFunction the mapping function to map a value from a String.
	 *
	 * @return a value of type T
	 */
	<T> T extractValue(String xPath, Function<String, T> mappingFunction);

	/**
	 * extracts a list of Strings.
	 *
	 * @param xPath The xpath.
	 *
	 * @return a list of Strings
	 */
	List<String> extractValues(String xPath);

	/**
	 * extracts a list of values from the element texts.
	 *
	 * @param <T>             The type of the value.
	 * @param xPath           The xpath.
	 * @param mappingFunction the mapping function to map a value from a String.
	 *
	 * @return a value of type T
	 */
	<T> List<T> extractValues(String xPath, Function<String, T> mappingFunction);
}

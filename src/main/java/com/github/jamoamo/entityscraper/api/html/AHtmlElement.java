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
package com.github.jamoamo.entityscraper.api.html;

import java.util.Iterator;
import java.util.List;

/**
 * Abstract representation of an HTML element. Custom {@link IParser} implementations are to provide their own
 * implementations of this class.
 *
 * @author James Amoore
 */
public abstract class AHtmlElement extends AHtmlNode
{
	/**
	 * Return the name of the element.
	 * 
	 * @return the name of the element (the tag name).
	 */
	public abstract String getElementName();

	/**
	 * Returns the Attribute with the provided name.
	 *
	 * @param attribute the name of the attribute to return.
	 *
	 * @return the attribute with the provided name.
	 */
	public abstract AHtmlAttribute getAttribute(String attribute);

	/**
	 * Return all the attributes of the element.
	 * 
	 * @return all the element's attributes as a {@link List}.
	 */
	public abstract List<AHtmlAttribute> getAttributes();

	/**
	 * Return the child elements of this element with a specific tag.
	 * 
	 * @param tag the name of the tag of the elements to return.
	 *
	 * @return all of the element's children elements with the provided tag name as a {@link List}.
	 */
	public abstract List<AHtmlElement> getElements(String tag);

	/**
	 * Return all the child elements of the element.
	 * 
	 * @return all of the element's children elements as a {@link List}.
	 */
	public abstract List<AHtmlElement> getAllElements();

	/**
	 * Return the element's text.
	 * 
	 * @return The text in the element
	 */
	public abstract String getText();

	/**
	 * Returns the sibling nodes that precede the element.
	 * 
	 * @return the siblings that precede.
	 */
	public abstract Iterator getSiblingsBefore();
	
	/**
	 * Returns the sibling nodes that follow the element.
	 * 
	 * @return the siblings that follow.
	 */
	public abstract Iterator getSiblingsAfter();
	
	/**
	 * Returns the sibling elements that precede the element.
	 * 
	 * @return the sibling elements that precede.
	 */
	public abstract Iterator getSiblingElementsBefore();
	
	/**
	 * Returns the sibling elements that follow the element.
	 * 
	 * @return the sibling elements that follow.
	 */
	public abstract Iterator getSiblingElementsAfter();
}

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

/**
 * A web element.
 * @author James Amoore
 */
public abstract class AElement
{
	/**
	 * Gets an attribute of the element by name.
	 * @param attribute the attribute name
	 * @return the value of the attribute
	 */
	public abstract String getAttribute(String attribute);
	
	/**
	 * Get the text from the element.
	 * @return element text.
	 */
	public abstract String getElementText();

	/**
	 * Finds a sub-element using the xpath.
	 * @param path the xpath.
	 * @return the sub-element
	 */
	public abstract AElement findElement(String path);

	/**
	 * Finds a list of sub-element using the xpath.
	 * @param path the xpath.
	 * @return list of sub-elements
	 */
	public abstract List<? extends AElement> findElements(String path);
	
	/**
	 * Clicks the element.
	 */
	public abstract void click();
	
	/**
	 * Enters Text to the element.
	 * @param text the text to enter in the element
	 */
	public abstract void enterText(String text);

	/**
	 * Get children by tag name.
	 * @param childElementType the tag.
	 * @return all the children of the element that have the given tag.
	 */
	public abstract List<? extends AElement> getChildrenByTag(String childElementType);

	/**
	 * Get the tag of the element.
	 * @return the element's tag
	 */
	public abstract String getTag();
	
	/**
	 * Indicates if the element exists in the current context.
	 * @return true if the element exists
	 */
	public abstract boolean exists();

}

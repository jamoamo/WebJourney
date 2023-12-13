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
package com.github.jamoamo.webjourney.reserved.entity;

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.net.URL;
import java.util.List;

/**
 * Reads a value from a source.
 * @author James Amoore
 */
public interface IValueReader
{
	/**
	 * Get the current URL.
	 * @return the current url.
	 */
	String getCurrentUrl();
	
	/**
	 * Get the text of the element identified by the xpath.
	 * @param xPath the element xpath
	 * @return the element text
	 */
	String getElementText(String xPath);
	
	/**
	 * Get the text of the elements identified by the xpath.
	 * @param xPath the element xpath
	 * @return the element texts
	 */
	List<String> getElementTexts(String xPath);
	
	/**
	 * Get the element identified by the xpath.
	 * @param xPath the element xpath
	 * @return the element
	 */
	AElement getElement(String xPath);
	
	/**
	 * Get the elements identified by the xpath.
	 * @param xPath the element xpath
	 * @return the elements
	 */
	List<? extends AElement> getElements(String xPath);
	
	/**
	 * Get the attribute identified by the element xpath and attribute name.
	 * @param xPath the element xpath
	 * @param attr the arribute name
	 * @return the element attribute value
	 */
	String getAttribute(String xPath, String attr);
	
	/**
	 * Navigates to the provided url.
	 * @param url the target url
	 */
	void navigateTo(URL url);
	
	/**
	 * Navigates back.
	 */
	void navigateBack();
	
	/**
	 * Get the browser used by the value reader.
	 * @return the browser.
	 */
	IBrowser getBrowser();
	
	/**
	 * Opens a new browser window.
	 */
	void openNewWindow();
	
	/**
	 * Closes the current browser window.
	 */
	void closeWindow();
}

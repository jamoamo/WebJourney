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
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	String getCurrentUrl() throws XValueReaderException;
	
	/**
	 * Get the text of the element identified by the xpath.
	 * @param xPath the element xpath
	 * @param optional if the element is optional
	 * @return the element text
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	String getElementText(String xPath, boolean optional) throws XValueReaderException;
	
	/**
	 * Get the text of the elements identified by the xpath.
	 * @param xPath the element xpath
	 * @return the element texts
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	List<String> getElementTexts(String xPath) throws XValueReaderException;
	
	/**
	 * Get the element identified by the xpath.
	 * @param xPath the element xpath
	 * @param optional if the element is optional
	 * @return the element
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	AElement getElement(String xPath, boolean optional) throws XValueReaderException;
	
	/**
	 * Get the elements identified by the xpath.
	 * @param xPath the element xpath
	 * @return the elements
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	List<? extends AElement> getElements(String xPath) throws XValueReaderException;
	
	/**
	 * Get the attribute identified by the element xpath and attribute name.
	 * @param xPath the element xpath
	 * @param attr the arribute name
	 * @return the element attribute value
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	String getAttribute(String xPath, String attr) throws XValueReaderException;
	
	/**
	 * Get the attribute identified by the element xpath and attribute name.
	 * @param xPath the element xpath
	 * @param attr the arribute name
	 * @return the element attribute value
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	List<String> getAttributes(String xPath, String attr) throws XValueReaderException;
	
	/**
	 * Navigates to the provided url.
	 * @param url the target url
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	void navigateTo(URL url) throws XValueReaderException;
	
	/**
	 * Navigates back.
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	void navigateBack() throws XValueReaderException;
	
	/**
	 * Get the browser used by the value reader.
	 * @return the browser.
	 */
	IBrowser getBrowser();
	
	/**
	 * Opens a new browser window.
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	void openNewWindow() throws XValueReaderException;
	
	/**
	 * Closes the current browser window.
	 * @throws com.github.jamoamo.webjourney.reserved.entity.XValueReaderException if there's an error
	 */
	void closeWindow() throws XValueReaderException;
}

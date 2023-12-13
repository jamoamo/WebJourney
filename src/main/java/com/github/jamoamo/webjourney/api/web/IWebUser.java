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

/**
 *
 * @author James Amoore
 */
public interface IWebUser
{
	/**
	 * Opens a new browser window.
	 */
	void openNewWindow();
	
	/**
	 * Closes a browser window.
	 */
	void closeWindow();
	
	/**
	 * Switches to the window with the provided title.
	 * @param windowName the name of the window
	 */
	void switchToWindow(String windowName);
	
	/**
	 * Select a button.
	 * @param xPath the xPath that identifies the button.
	 */
	void selectButton(String xPath);
	
	/**
	 * Enter a value into an element.
	 * @param xPath the xPath that identifies the button.
	 * @param value the value to enter in the element
	 */
	void enterValueInElement(String xPath, String value);
	
	/**
	 * Retrieve the value of an element.
	 * @param xPath the xpath that identifies the element
	 * @return the element value
	 */
	String getElementValue(String xPath);
}

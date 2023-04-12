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

import java.net.URL;
import java.util.List;
import java.util.function.Function;
import org.openqa.selenium.WebElement;
/**
 * A browser instance.
 *
 * @author James Amoore
 */
public interface IBrowser 
{
    /**
     * Navigates to the URL.
     *
     * @param url The URL to navigate to.
     */
    void navigateToUrl(URL url);

    /**
     * Fill Text in the Element.
     *
     * @param xPathExpression The xpath to the tet field to fill.
     * @param value The value to fill.
     */
    void fillElement(String xPathExpression, String value);

    /**
     * Gets the text from an element.
     *
     * @param xPathExpression The xPath expression to the element.
     * @return test from the element.
     */
    String getElementText(String xPathExpression);

    /**
     * Get list of texts from the elements.
     *
     * @param xPathExpression The xpath expression to the elements.
     * @return a list of text from the elements
     */
    List<String> getElementTexts(String xPathExpression);

    /**
     * Click an element.
     *
     * @param xPathExpression XPath to the element.
     */
    void clickElement(String xPathExpression);

    /**
     * Click an element.
     *
     * @param xPathExpression XPath to the element.
     * @param ignoreIfNotPresent determines whether to ignore the error if the
     * element isn't present.
     */
    void clickElement(String xPathExpression, boolean ignoreIfNotPresent);

    /**
     * Quit the browser.
     */
    void exit();

    /**
     * Wait on elements to appear on the page.
     *
     * @param xPathExpressions The xPath
     */
    void waitForAllElements(String... xPathExpressions);

    /**
     * Get the Entities mapped from the elements found via the xpath expression.
     *
     * @param <T> The Entity type
     * @param xPath The xpath expression.
     * @param function The function to map a web element to an entity.
     * @return the list of entities.
     */
    <T> List<T> getElementEntities(String xPath, Function<WebElement, T> function);

    /**
     * Find an element that matches the xpath expression.
     *
     * @param xPath xPath expression to the element.
     * @return the found Element.
     */
    AElement getElement(String xPath);

    /**
     * Find a list of elements that match the xpath expression.
     *
     * @param xPath xPath expression to the elements.
     * @return the found Elements.
     */
    List<? extends AElement> getElements(String xPath);
}

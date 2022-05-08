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
package com.github.jamoamo.entityscraper.xpath.jaxen;

import com.github.jamoamo.entityscraper.html.AHtmlAttribute;
import com.github.jamoamo.entityscraper.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.html.AHtmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public class TestHtmlDocument extends AHtmlDocument
{
	private final AHtmlElement html;
	
	public TestHtmlDocument()
	{
		List<AHtmlElement> row2Elements = new ArrayList<>();
		row2Elements.add(new TestHtmlElement("td", "Value1", Collections.singletonList(new TestHtmlAttribute("attr", "nameValue")), Collections.EMPTY_LIST));
		row2Elements.add(new TestHtmlElement("td", "Value2"));
		AHtmlElement row2 = new TestHtmlElement("tr", "", new ArrayList<>(), row2Elements);
		
		List<AHtmlElement> row1Elements = new ArrayList<>();
		row1Elements.add(new TestHtmlElement("td", "Header1", Collections.singletonList(new TestHtmlAttribute("attr", "nameHeader")), Collections.EMPTY_LIST));
		row1Elements.add(new TestHtmlElement("td", "Header2"));
		
		AHtmlElement row1 = new TestHtmlElement("tr", "", new ArrayList<>(), row1Elements);
		
		List<AHtmlElement> tableElements = new ArrayList<>();
		tableElements.add(row1);
		tableElements.add(row2);
		AHtmlElement table = new TestHtmlElement("table", "", new ArrayList<>(), tableElements);
		
		List<AHtmlElement> htmlElements = new ArrayList<>();
		htmlElements.add(table);
		
		AHtmlElement bodyElement = new TestHtmlElement("body", new ArrayList<AHtmlAttribute>(), htmlElements);
		
		html = new TestHtmlElement("html", "", new ArrayList<>(), Collections.singletonList(bodyElement));
	}

	@Override
	public AHtmlElement getHtmlElement()
	{
		return html;
	}

	@Override
	public AHtmlElement getRootElement()
	{
		return new TestHtmlElement("", Collections.singletonList(html));
	}
	
}

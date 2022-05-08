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
package com.github.jamoamo.entityscraper.html.jsoup;

import com.github.jamoamo.entityscraper.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.html.AHtmlElement;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class JSoupParserTest
{
	@Test
	public void testParse_File()
			  throws Exception
	{
		URL url = getClass().getClassLoader().getResource("testpage.html");
		JSoupParser instance = new JSoupParser();
		AHtmlDocument result = instance.parse(new File(url.toURI()), Charset.forName("UTF-8"));
		AHtmlElement htmlElement = result.getHtmlElement();
		assertEquals("html", htmlElement.getElementName());
		
		List<AHtmlElement> bodyElements = htmlElement.getElements("body");
		AHtmlElement bodyElement = bodyElements.get(0);
		assertEquals("body", bodyElement.getElementName());
		
		List<AHtmlElement> h1Elements = bodyElement.getElements("h1");
		assertEquals(1, h1Elements.size());
		AHtmlElement h1Element = h1Elements.get(0);
		assertEquals("h1", h1Element.getElementName());
		assertEquals("Test Page", h1Element.getText());
		
		List<AHtmlElement> h2Elements = bodyElement.getElements("h2");
		assertEquals(1, h2Elements.size());
		AHtmlElement h2Element = h2Elements.get(0);
		assertEquals("h2", h2Element.getElementName());
		assertEquals("Table", h2Element.getText());
		
		assertEquals("#root", result.getRootElement().getElementName());
	}
	
}

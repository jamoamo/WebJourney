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
package com.github.jamoamo.entityscraper.reserved.xpath.jaxen;

import com.github.jamoamo.entityscraper.reserved.xpath.jaxen.HtmlDocumentXPath;
import com.github.jamoamo.entityscraper.api.html.AHtmlAttribute;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author James Amoore
 */
public class HtmlDocumentXPathTest
{
	@Test
	public void testEvaluate_index()
			  throws Exception
	{
		HtmlDocumentXPath instance = new HtmlDocumentXPath("/html/body/table/tr[2]/td");
		List<AHtmlElement> evaluate = (List<AHtmlElement>) instance.evaluate(new TestHtmlDocument());
		assertEquals(2, evaluate.size());
		assertEquals("Value1", evaluate.get(0).getText());
		assertEquals("Value2", evaluate.get(1).getText());
	}
	
	@Test
	public void testEvaluate_all()
			  throws Exception
	{
		HtmlDocumentXPath instance = new HtmlDocumentXPath("/html/body/table/tr/td");
		List<AHtmlElement> evaluate = (List<AHtmlElement>) instance.evaluate(new TestHtmlDocument());
		assertEquals(4, evaluate.size());
		assertEquals("Header1", evaluate.get(0).getText());
		assertEquals("Header2", evaluate.get(1).getText());
		assertEquals("Value1", evaluate.get(2).getText());
		assertEquals("Value2", evaluate.get(3).getText());
	}
	
	@Test
	public void testEvaluate_attr()
			  throws Exception
	{
		HtmlDocumentXPath instance = new HtmlDocumentXPath("/html/body/table/tr/td[@attr]");
		List<AHtmlElement> evaluate = (List<AHtmlElement>) instance.evaluate(new TestHtmlDocument());
		assertEquals(2, evaluate.size());
		assertEquals("Header1", evaluate.get(0).getText());
		assertEquals("Value1", evaluate.get(1).getText());
	}
	
	@Test
	public void testEvaluate_attrValue()
			  throws Exception
	{
		HtmlDocumentXPath instance = new HtmlDocumentXPath("/html/body/table/tr/td[@attr]/@attr");
		List<AHtmlAttribute> evaluate = (List<AHtmlAttribute>) instance.evaluate(new TestHtmlDocument());
		assertEquals(2, evaluate.size());
		assertEquals("nameHeader", evaluate.get(0).getValue());
		assertEquals("nameValue", evaluate.get(1).getValue());
	}
}

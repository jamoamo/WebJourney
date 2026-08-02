/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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
package io.github.jamoamo.webjourney.test.mock;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests of the text node support added to {@link MockElement} for offline testing of
 * {@code @ExtractTextValue}.
 */
public class MockElementTextNodeTest
{
	@Test
	public void testTextNode_isTextNode()
	{
		MockElement node = MockElement.textNode("some text");
		assertTrue(node.isTextNode());
		assertEquals("some text", node.getElementText());
	}

	@Test
	public void testElement_isNotTextNode()
	{
		MockElement element = MockElement.tag("table");
		assertTrue(!element.isTextNode());
	}

	@Test
	public void testGetTextNodeValues_followingSiblingIndexed() throws Exception
	{
		MockElement table = MockElement.tag("table");
		MockElement.tag("div")
			.child(table)
			.child(MockElement.textNode("End of over 1: 4 runs scored"));

		List<String> values = table.getTextNodeValues("following-sibling::text()[1]");

		assertEquals(1, values.size());
		assertEquals("End of over 1: 4 runs scored", values.get(0));
	}

	@Test
	public void testGetTextNodeValues_followingSibling_missing() throws Exception
	{
		MockElement table = MockElement.tag("table");
		MockElement.tag("div")
			.child(table);

		List<String> values = table.getTextNodeValues("following-sibling::text()[1]");

		assertTrue(values.isEmpty());
	}

	@Test
	public void testGetTextNodeValues_followingSibling_noParent() throws Exception
	{
		MockElement table = MockElement.tag("table");

		List<String> values = table.getTextNodeValues("following-sibling::text()[1]");

		assertTrue(values.isEmpty());
	}

	@Test
	public void testGetTextNodeValues_directChildTextNodes_nodeSet() throws Exception
	{
		MockElement parent = MockElement.tag("div")
			.child(MockElement.textNode("a"))
			.child(MockElement.tag("span").text("ignored"))
			.child(MockElement.textNode("b"));

		List<String> values = parent.getTextNodeValues("text()");

		assertEquals(List.of("a", "b"), values);
	}

	@Test
	public void testGetTextNodeValues_unrecognisedXPath_throws()
	{
		MockElement element = MockElement.tag("div");

		org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
			() -> element.getTextNodeValues("//not/supported"));
	}

	@Test
	public void testGetTextNodeValues_indexedDirectChild_unsupported_throws()
	{
		MockElement element = MockElement.tag("div")
			.child(MockElement.textNode("a"));

		org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
			() -> element.getTextNodeValues("text()[1]"));
	}

	@Test
	public void testGetTextNodeValues_followingSibling_nonNumericIndex_throws()
	{
		MockElement table = MockElement.tag("table");
		MockElement.tag("div")
			.child(table)
			.child(MockElement.textNode("End of over 1: 4 runs scored"));

		org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
			() -> table.getTextNodeValues("following-sibling::text()[last()]"));
	}

	@Test
	public void testGetElementText_includesTextNodeChildren() throws Exception
	{
		MockElement element = MockElement.tag("p")
			.child(MockElement.textNode("Hello"))
			.child(MockElement.tag("b").text("bold"))
			.child(MockElement.textNode("World"));

		assertEquals("Hello World", element.getElementText());
	}

}

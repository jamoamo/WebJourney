/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A mock element backed by an in-memory tree. Provides simple XPath subset and handlers.
 */
public final class MockElement extends AElement
{
	private final String tag;
	private final Map<String, String> attributes;
	private String text;
	private final List<MockElement> children;

	private Consumer<MockElement> clickHandler;
	private Consumer<TextEntry> textEntryHandler;

	MockElement(String tag)
	{
		this.tag = tag;
		this.attributes = new LinkedHashMap<>();
		this.children = new ArrayList<>();
		this.text = "";
	}

	public static MockElement tag(String tag)
	{
		return new MockElement(tag);
	}

	public MockElement attr(String name, String value)
	{
		this.attributes.put(name, value);
		return this;
	}

	public MockElement text(String text)
	{
		this.text = text == null ? "" : text;
		return this;
	}

	public MockElement child(MockElement child)
	{
		this.children.add(child);
		return this;
	}

	public MockElement onClick(Consumer<MockElement> handler)
	{
		this.clickHandler = handler;
		return this;
	}

	public MockElement onTextEntry(Consumer<TextEntry> handler)
	{
		this.textEntryHandler = handler;
		return this;
	}

	@Override
	public String getAttribute(String attribute) throws XElementDoesntExistException
	{
		return this.attributes.get(attribute);
	}

	@Override
	public String getElementText() throws XElementDoesntExistException
	{
		return this.text;
	}

	@Override
	public AElement findElement(String path) throws XElementDoesntExistException
	{
		return findElement(path, false);
	}

	@Override
	public AElement findElement(String path, boolean optional) throws XElementDoesntExistException
	{
		List<MockElement> found = findAllByXPath(path);
		if(found.isEmpty())
		{
			return optional ? new NullElement() : new MissingElement();
		}
		return found.get(0);
	}

	@Override
	public List<? extends AElement> findElements(String path) throws XElementDoesntExistException
	{
		return Collections.unmodifiableList(findAllByXPath(path));
	}

	@Override
	public void click() throws XElementDoesntExistException
	{
		if(this.clickHandler != null)
		{
			this.clickHandler.accept(this);
		}
	}

	@Override
	public void enterText(String text) throws XElementDoesntExistException
	{
		this.attributes.put("value", Objects.toString(text, ""));
		if(this.textEntryHandler != null)
		{
			this.textEntryHandler.accept(new TextEntry(this, text));
		}
	}

	@Override
	public List<? extends AElement> getChildrenByTag(String childElementType) throws XElementDoesntExistException
	{
		return this.children.stream().filter(c -> Objects.equals(c.tag, childElementType)).collect(Collectors.toList());
	}

	@Override
	public String getTag() throws XElementDoesntExistException
	{
		return this.tag;
	}

	@Override
	public boolean exists()
	{
		return true;
	}

	List<MockElement> getChildren()
	{
		return this.children;
	}

	Map<String, String> getAttributes()
	{
		return this.attributes;
	}

	private List<MockElement> findAllByXPath(String xPath)
	{
		// Minimal subset: //tag[@attr='value'] and descendant search
		if(xPath == null || xPath.isEmpty())
		{
			return List.of();
		}
		String expr = xPath.trim();
		if(!expr.startsWith("//"))
		{
			return List.of();
		}
		expr = expr.substring(2);
		String tagName;
		String attrName = null;
		String attrValue = null;
		int bracketStart = expr.indexOf('[');
		if(bracketStart >= 0 && expr.endsWith("]"))
		{
			tagName = expr.substring(0, bracketStart);
			String inside = expr.substring(bracketStart + 1, expr.length() - 1).trim();
			// format: @attr='value'
			if(inside.startsWith("@"))
			{
				int eq = inside.indexOf('=');
				if(eq > 1)
				{
					attrName = inside.substring(1, eq).trim();
					String v = inside.substring(eq + 1).trim();
					if(v.startsWith("'") && v.endsWith("'"))
					{
						attrValue = v.substring(1, v.length() - 1);
					}
				}
			}
		}
		else
		{
			tagName = expr;
		}
		List<MockElement> results = new ArrayList<>();
		collectDescendantsByTag(this, tagName, results);
		if(attrName != null)
		{
			return results.stream()
				.filter(e -> Objects.equals(e.attributes.get(attrName), attrValue))
				.collect(Collectors.toList());
		}
		return results;
	}

	private static void collectDescendantsByTag(MockElement root, String tag, List<MockElement> into)
	{
		if(Objects.equals(root.tag, tag))
		{
			into.add(root);
		}
		for(MockElement c : root.children)
		{
			collectDescendantsByTag(c, tag, into);
		}
	}

	public static final class TextEntry
	{
		public final MockElement element;
		public final String text;
		TextEntry(MockElement element, String text)
		{
			this.element = element;
			this.text = text;
		}
	}
}
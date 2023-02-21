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

import com.github.jamoamo.entityscraper.api.html.AHtmlAttribute;
import com.github.jamoamo.entityscraper.api.html.AHtmlDocument;
import com.github.jamoamo.entityscraper.api.html.AHtmlElement;
import com.github.jamoamo.entityscraper.api.html.AHtmlTextNode;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenConstants;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A navigator of an {@link AHtmlDocument}.
 * 
 * @author James Amoore
 */
public final class HtmlDocumentNavigator
	 extends DefaultNavigator
	 implements NamedAccessNavigator
{
	private static final HtmlDocumentNavigator INSTANCE = new HtmlDocumentNavigator();
	
	private final Logger logger = LoggerFactory.getLogger(HtmlDocumentNavigator.class);

	/**
	 * Gets the HtmlDocumentNavigator instance.
	 * @return the instance
	 */
	public static HtmlDocumentNavigator getInstance()
	{
		return INSTANCE;
	}

	/**
	 * Returns an empty namespace. Name spaces are not supported in the document model.
	 *
	 * @param o the object
	 *
	 * @return the empty namespace.
	 */
	@Override
	public String getElementNamespaceUri(Object o)
	{
		return "";
	}

	@Override
	public String getElementName(Object o)
	{
		if(o instanceof AHtmlElement)
		{
			return ((AHtmlElement) o).getElementName();
		}
		else
		{
			return "";
		}
	}

	@Override
	public String getElementQName(Object o)
	{
		return getElementName(o);
	}

	@Override
	public String getAttributeNamespaceUri(Object o)
	{
		return "";
	}

	@Override
	public String getAttributeName(Object o)
	{
		if(o instanceof AHtmlAttribute)
		{
			return ((AHtmlAttribute) o).getAttributeName();
		}
		else
		{
			return "";
		}
	}

	@Override
	public String getAttributeQName(Object o)
	{
		return getAttributeName(o);
	}

	@Override
	public boolean isDocument(Object o)
	{
		return o instanceof AHtmlDocument;
	}

	@Override
	public boolean isElement(Object o)
	{
		return o instanceof AHtmlElement;
	}

	@Override
	public boolean isAttribute(Object o)
	{
		return o instanceof AHtmlAttribute;
	}

	@Override
	public boolean isNamespace(Object o)
	{
		return false;
	}

	@Override
	public boolean isComment(Object o)
	{
		return false;
	}

	@Override
	public boolean isText(Object o)
	{
		return o instanceof AHtmlTextNode || o instanceof String;
	}

	@Override
	public boolean isProcessingInstruction(Object o)
	{
		return false;
	}

	@Override
	public String getCommentStringValue(Object o)
	{
		return "";
	}

	@Override
	public String getElementStringValue(Object o)
	{
		if(o instanceof AHtmlElement)
		{
			AHtmlElement element = (AHtmlElement) o;
			this.logger.info("Getting Element text : " + element.getText());
			return element.getText();
		}
		else if (o instanceof AHtmlTextNode)
		{
			AHtmlTextNode textNode = (AHtmlTextNode)o;
			this.logger.info("Getting TextNode text : " + textNode.getText());
			return textNode.getText();
		}
		else if(o instanceof String)
		{
			this.logger.info("Getting String text : " + o.toString());
			return (String) o;
		}
		else
		{
			return String.valueOf(o);
		}
	}

	@Override
	public String getAttributeStringValue(Object o)
	{
		if(o instanceof AHtmlAttribute)
		{
			AHtmlAttribute attr = (AHtmlAttribute) o;
			return attr.getValue();
		}
		return "";
	}

	@Override
	public String getNamespaceStringValue(Object o)
	{
		return "";
	}

	@Override
	public String getTextStringValue(Object o)
	{
		return getElementStringValue(o);
	}

	@Override
	public String getNamespacePrefix(Object o)
	{
		return "";
	}

	@Override
	public XPath parseXPath(String string)
		 throws SAXPathException
	{
		return new HtmlDocumentXPath(string);
	}

	@Override
	public Iterator getChildAxisIterator(Object o, String localName, String namespacePrefix, String namespaceUri)
		 throws UnsupportedAxisException
	{
		this.logger.info("Getting children with local name: " + localName);
		if(o instanceof AHtmlElement)
		{
			List children = ((AHtmlElement) o).getElements(localName);
			return children.iterator();
		}
		else
		{
			return JaxenConstants.EMPTY_ITERATOR;
		}
	}
	
	@Override
	public Iterator getChildAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		if(isElement(contextNode))
		{
			AHtmlElement element = (AHtmlElement) contextNode;
			this.logger.info("Getting all children for node: " + element.getElementName());
			List children = element.getAllElements();
			children.add(element.getText());
			return children.iterator();
		}
		else
		{
			return JaxenConstants.EMPTY_ITERATOR;
		}
	}

	@Override
	public Iterator getAttributeAxisIterator(Object o, String localName, String namespacePrefix, String namespaceUri)
		 throws UnsupportedAxisException
	{
		if(o instanceof AHtmlElement)
		{
			AHtmlAttribute attribute = ((AHtmlElement) o).getAttribute(localName);
			return Collections.singletonList(attribute).iterator();
		}
		else
		{
			return JaxenConstants.EMPTY_ITERATOR;
		}
	}
	
	@Override
	public Iterator getAttributeAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		if(isElement(contextNode))
		{
			AHtmlElement element = (AHtmlElement) contextNode;
			List<AHtmlAttribute> attributes = element.getAttributes();
			return attributes.iterator();
		}
		return JaxenConstants.EMPTY_ITERATOR;
	}

	@Override
	public Object getElementById(Object contextNode, String elementId)
	{
		if(isElement(contextNode))
		{
			List<AHtmlElement> elements = ((AHtmlElement) contextNode).getAllElements()
				 .stream()
				 .filter(elem -> elem.getAttribute("id").getValue()
					  .equals(elementId))
				 .collect(Collectors.toList());
			return elements;
		}
		else
		{
			return Collections.emptyList();
		}
	}

	@Override
	public Object getDocumentNode(Object contextNode)
	{
		if(contextNode instanceof AHtmlDocument)
		{
			AHtmlDocument doc = (AHtmlDocument) contextNode;
			return doc.getRootElement();
		}
		else
		{
			return contextNode;
		}
	}

	@Override
	public Iterator getNamespaceAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		return JaxenConstants.EMPTY_ITERATOR;
	}

	@Override
	public Iterator getPrecedingSiblingAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		AHtmlElement element = (AHtmlElement)contextNode;
		this.logger.info("Getting Preceding siblings for: " + element.getElementName());
		Iterator siblingsBefore = element.getSiblingElementsBefore();
		return siblingsBefore;
	}

	@Override
	public Iterator getFollowingSiblingAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		AHtmlElement element = (AHtmlElement)contextNode;
		this.logger.info("Getting Following siblings for: " + element.getElementName());
		Iterator siblingsAfter = element.getSiblingElementsAfter();
		return siblingsAfter;
	}

	@Override
	public Iterator getPrecedingAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		Iterator siblingsBefore = ((AHtmlElement)contextNode).getSiblingsBefore();
		return siblingsBefore;
	}

	@Override
	public Iterator getFollowingAxisIterator(Object contextNode)
		 throws UnsupportedAxisException
	{
		Iterator siblingsAfter = ((AHtmlElement)contextNode).getSiblingsAfter();
		return siblingsAfter;
	}
}

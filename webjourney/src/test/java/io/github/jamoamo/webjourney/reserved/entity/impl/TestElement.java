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
package io.github.jamoamo.webjourney.reserved.entity.impl;

import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.XElementDoesntExistException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.function.Failable;

/**
 *
 * @author James Amoore
 */
public class TestElement
	 extends AElement
{
	 private final String text;
	 private AElement[] subElements;
	 private final String elementName;
	 private final Map<String, String> attributes;

	 public TestElement(String text)
	 {
		  this.elementName = "";
		  this.attributes = new TreeMap<>();
		  this.text = text;
		  this.subElements = new AElement[]
		  {
		  };
	 }

	 public TestElement(String elementName, Map<String, String> attributes, String text)
	 {
		  this.elementName = elementName;
		  this.attributes = attributes;
		  this.text = text;
		  this.subElements = new AElement[]
		  {
		  };
	 }

	 public TestElement(String text, AElement[] subElements)
	 {
		  this.elementName = "";
		  this.attributes = new TreeMap<>();
		  this.text = text;
		  this.subElements = subElements;
	 }
	 
	 public TestElement(String elementName, Map<String, String> attributes, AElement[] subElements, String text)
	 {
		  this.elementName = elementName;
		  this.attributes = attributes;
		  this.text = text;
		  this.subElements = subElements;
	 }

	 @Override
	 public String getElementText()
	 {
		  return text;
	 }

	 @Override
	 public AElement findElement(String path, boolean optional)
	 {
		  return this.findElement(path);
	 }

	 @Override
	 public AElement findElement(String path)
	 {
		  if(!path.startsWith("//"))
		  {
				return null;
		  }

		  path = path.substring(2);

		  if(!path.startsWith("div"))
		  {
				return null;
		  }

		  path = path.substring(3);

		  if(!path.startsWith("[") && !path.endsWith("]"))
		  {
				return null;
		  }

		  path = path.substring(1, path.length() - 1);

		  if(!path.startsWith("@id"))
		  {
				return null;
		  }

		  String id = path.substring(5, path.length() - 1);

		  Optional<AElement> findFirst =
				Failable.stream(Arrays.stream(subElements))
					 .filter(element -> elementMatch(element, id))
					 .stream()
					 .findFirst();

		  return findFirst.orElse(null);
	 }

	 @Override
	 public List<? extends AElement> findElements(String path)
	 {
		  if(!path.startsWith("//"))
		  {
				return null;
		  }

		  path = path.substring(2);

		  if(!path.startsWith("div"))
		  {
				return null;
		  }

		  path = path.substring(3);

		  if(!path.startsWith("[") && !path.endsWith("]"))
		  {
				return null;
		  }

		  path = path.substring(1, path.length() - 1);

		  if(!path.startsWith("@id"))
		  {
				return null;
		  }

		  String id = path.substring(5, path.length() - 1);

		  List<AElement> list =
				Failable.stream(Arrays.stream(subElements))
					 .filter(element -> elementMatch(element, id))
					 .collect(Collectors.toList());

		  return list;
	 }

	public boolean elementMatch(AElement element, String id)
		 throws XElementDoesntExistException
	{
		return element.getAttribute("id") != null && element.getAttribute("id")
			 .equals(id);
	}

	 @Override
	 public String getAttribute(String attribute)
	 {
		  return attributes.get(attribute);
	 }

	 @Override
	 public void click()
	 {

	 }

	 @Override
	 public void enterText(String text)
	 {

	 }

	 @Override
	 public List<? extends AElement> getChildrenByTag(String childElementType)
	 {
		  return Failable.stream(Arrays.stream(this.subElements))
				.filter(elem -> elem.getTag()
					 .equals(childElementType))
				.stream()
				.toList();
	 }

	 @Override
	 public String getTag()
	 {
		  return this.elementName;
	 }

	 @Override
	 public boolean exists()
	 {
		  return true;
	 }
	 
	 public void addElement(AElement element)
	 {
		 AElement[] newArr = Arrays.copyOf(subElements, subElements.length + 1);
		 newArr[newArr.length - 1] = element;
		 subElements = newArr;
	 }
	 
	 public Stream<TestElement> streamSubElements()
	 {
		 return Arrays.stream(subElements).map(e -> (TestElement)e);
	 }

}

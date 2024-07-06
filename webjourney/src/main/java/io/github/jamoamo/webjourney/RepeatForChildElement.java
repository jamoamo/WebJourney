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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.annotation.form.Element;
import io.github.jamoamo.webjourney.api.web.AElement;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 *
 * @author James Amoore
 */
class RepeatForChildElement implements IRepeatable<AElement>
{
	private final String childElementType;
	private final String elementName;
	private final Class pageClass;

	RepeatForChildElement(Class pageClass, String elementName, String childElementTag)
	{
		this.pageClass = pageClass;
		this.elementName = elementName;
		this.childElementType = childElementTag;
	}

	@Override
	public Iterable<AElement> repeatIterable(IJourneyContext context)
			  throws JourneyException
	{
		IBrowser browser = context.getBrowser();
		Field elementField = FieldUtils.getField(this.pageClass, this.elementName, true);
		if(elementField == null)
		{
			throw new JourneyException("Not a field: " + this.elementName);
		}

		Element element = elementField.getAnnotation(Element.class);
		if(element == null)
		{
			throw new JourneyException("Not an element: " + this.elementName);
		}
		
		try
		{
			List<? extends AElement> childElementsByTag = browser.getActiveWindow()
				.getCurrentPage()
				.getElement(this.elementName)
				.getChildrenByTag(this.childElementType);
			
			return childElementsByTag.stream().map(elem -> (AElement) elem).toList();
		}
		catch(XWebException e)
		{
			throw new JourneyException(e);
		}
	}

}

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
package com.github.jamoamo.webjourney;

import com.github.jamoamo.webjourney.annotation.form.Button;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 *
 * @author James Amoore
 */
class ClickButtonAction extends AWebAction
{
	private final Class pageClass;
	private final String buttonName;
	
	ClickButtonAction(Object pageObject, String buttonName)
	{
		this.pageClass = pageObject.getClass();
		this.buttonName = buttonName;
	}
	
	ClickButtonAction(Class pageClass, String buttonName)
	{
		this.pageClass = pageClass;
		this.buttonName = buttonName;
	}
	
	@Override
	protected ActionResult executeAction(IBrowser browser)
	{
		Field buttonField = FieldUtils.getField(this.pageClass, this.buttonName, true);
		if(buttonField == null)
		{
			return ActionResult.FAILURE;
		}
		
		Button ef = buttonField.getAnnotation(Button.class);
		if(ef == null)
		{
			return ActionResult.FAILURE;
		}
		
		browser.clickElement(ef.xPath());
		return ActionResult.SUCCESS;
	}
	
}

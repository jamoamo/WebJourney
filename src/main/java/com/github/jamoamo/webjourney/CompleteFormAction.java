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

import com.github.jamoamo.webjourney.annotation.form.Form;
import com.github.jamoamo.webjourney.annotation.form.TextField;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author James Amoore
 */
class CompleteFormAction extends AWebAction
{
	private final Logger logger = LoggerFactory.getLogger(CompleteFormAction.class);

	private final Object form;
	private boolean submit = false;

	CompleteFormAction(Object form)
	{
		this.form = form;
	}

	@Override
	protected ActionResult executeAction(IJourneyContext context)
	{
		IBrowser browser = context.getBrowser();
		List<Field> textFields =
				  FieldUtils.getFieldsListWithAnnotation(this.form.getClass(), TextField.class);
		for(Field field : textFields)
		{
			setTextField(browser, field);
		}

		if(this.submit)
		{
			String submitXPath = this.form.getClass().getAnnotation(Form.class).submit();
			browser.clickElement(submitXPath);
		}
		return ActionResult.SUCCESS;
	}

	void setSubmitAfterwards()
	{
		this.submit = true;
	}

	private void setTextField(IBrowser browser, Field textField)
	{
		try
		{
			Object value = PropertyUtils.getSimpleProperty(this.form, textField.getName());
			if(value == null)
			{
				return;
			}
			TextField entityField = textField.getAnnotation(TextField.class);
			browser.fillElement(
					  entityField.xPath(),
					  value.toString());
		}
		catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException ex)
		{
			String logMessageFormat = "Could not set field [%s] on class [%s]";
			this.logger.error(String.format(
					  logMessageFormat,
					  textField.getName(),
					  this.form.getClass().getCanonicalName()));
		}
	}
}

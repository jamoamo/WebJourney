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

import io.github.jamoamo.webjourney.api.AWebAction;
import io.github.jamoamo.webjourney.api.IJourneyContext;
import io.github.jamoamo.webjourney.annotation.form.Form;
import io.github.jamoamo.webjourney.annotation.form.TextField;
import io.github.jamoamo.webjourney.api.web.IBrowser;
import io.github.jamoamo.webjourney.api.web.XWebException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	protected ActionResult executeActionImpl(IJourneyContext context)
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
			try
			{
				String submitXPath = this.form.getClass().getAnnotation(Form.class).submit();
				browser.getActiveWindow().getCurrentPage().getElement(submitXPath).click();
			}
			catch(XWebException ex)
			{
				throw new JourneyException(ex);
			}
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
			browser.getActiveWindow().getCurrentPage().getElement(entityField.xPath()).enterText(value.toString());
		}
		catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException | XWebException ex)
		{
			this.logger.atError().setMessage("Could not set a the value of a field")
				.addKeyValue("@timestamp", LocalDateTime.now().atZone(ZoneId.of("GMT")).toString())
				.addKeyValue("labels.field", textField.getName())
				.addKeyValue("labels.formClass", this.form.getClass().getCanonicalName())
				.addKeyValue("error.type", ex.getClass().getCanonicalName())
				.addKeyValue("error.message", ex.getMessage())
				.addKeyValue("error.stacktrace", ex.getStackTrace());
			throw new JourneyException(ex);
		}
	}

	@Override
	protected String getActionName()
	{
		return "Complete Form";
	}
}

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
package com.github.jamoamo.webjourney.reserved.step;

import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.annotation.form.Form;
import com.github.jamoamo.webjourney.annotation.form.TextField;
import com.github.jamoamo.webjourney.api.FormFill;
import com.github.jamoamo.webjourney.api.IJourneyContext;
import com.github.jamoamo.webjourney.reserved.log.LogMessage;
import com.github.jamoamo.webjourney.reserved.log.LogSeverity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtils;

/**
 *
 * @author James Amoore
 * @param <T> The Form entity to fill the form with.
 */
public final class FillFormStep<T> extends AJourneyStep
{
	private final T form;
	
	/**
	 * Creates a new instance.
	 * @param formFill The action.
	 */
	public FillFormStep(FormFill<T> formFill)
	{
		this.form = formFill.getForm();
	}

	/**
	 * Executes the step.
	 * @param context The journey context
	 */
	@Override
	public void executeStep(IJourneyContext context)
	{
		context.log(new LogMessage(LogSeverity.INFO, "Filling in form: " + this.form.getClass().getCanonicalName()));
		this.executeStep(this.form, context.getBrowser());
	}
	
	private void executeStep(T form, IBrowser browser)
	{
		Class<?> formClass = form.getClass();
		Form formAnnotation = formClass.getAnnotation(Form.class);
		browser.waitForAllElements(formAnnotation.submit());
		for(Field field : formClass.getDeclaredFields())
		{
			if(field.isAnnotationPresent(TextField.class))
			{
				field.setAccessible(true);
				fillField(field, browser);
			}
		}
		browser.clickElement(formAnnotation.submit());
	}

	private void fillField(Field field, IBrowser browser)
	{
		try
		{
			TextField textField = field.getAnnotation(TextField.class);
			String xPath = textField.xPath();
			browser.fillElement(xPath, BeanUtils.getSimpleProperty(form, field.getName()));
		}
		catch(IllegalAccessException |
			 NoSuchMethodException |
			 SecurityException |
			 InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}

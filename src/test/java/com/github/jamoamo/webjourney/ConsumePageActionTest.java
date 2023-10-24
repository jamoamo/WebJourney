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

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ConsumePageActionTest
{

	private static class TestElement extends AElement
	{
		private final String text;
		public TestElement(String text)
		{
			this.text = text;
		}

		@Override
		public String getElementText()
		{
			return text;
		}

		@Override
		public AElement findElement(String path)
		{
			return null;
		}

		@Override
		public List<? extends AElement> findElements(String path)
		{
			return null;
		}

		@Override
		public String getAttribute(String attribute)
		{
			return null;
		}
	}
	
	public ConsumePageActionTest()
	{
	}

	/**
	 * Test of executeAction method, of class ConsumePageAction.
	 */
	@Test
	public void testExecuteAction()
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		JourneyContext context = new JourneyContext();
		context.setBrowser(browser);
		
		Mockito.when(browser.getElement(any())).thenReturn(new TestElement("t1"));
		
		EntityConsumer consumer = new EntityConsumer();
		ConsumePageAction action = new ConsumePageAction(Entity.class, consumer);
		ActionResult result = action.executeAction(context);
		assertEquals(ActionResult.SUCCESS, result);
		
		assertEquals("t1", consumer.getEntity().getTestName());
	}
	
	@Test
	public void testConstructor_nullClass()
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		
		
		Mockito.when(browser.getElement(any())).thenReturn(new TestElement("t1"));
		
		EntityConsumer consumer = new EntityConsumer();
		NullPointerException exception =
				  assertThrows(NullPointerException.class, () -> new ConsumePageAction(null, consumer));
		assertEquals("Page Class should not be null.", exception.getMessage());
	}
	
	@Test
	public void testConstructor_nullConsumer()
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		
		
		Mockito.when(browser.getElement(any())).thenReturn(new TestElement("t1"));
		
		NullPointerException exception =
				  assertThrows(NullPointerException.class, () -> new ConsumePageAction(Entity.class, null));
		assertEquals("Page Consumer should not be null.", exception.getMessage());
	}
	
	private class EntityConsumer implements Consumer<Entity>
	{
		private Entity entity;
		
		@Override
		public void accept(Entity t)
		{
			this.entity = t;
		}
		
		public Entity getEntity()
		{
			return this.entity;
		}
		
	}
}

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
package io.github.jamoamo.webjourney.reserved.entity;

import io.github.jamoamo.webjourney.reserved.entity.IValueReader;
import io.github.jamoamo.webjourney.reserved.entity.EntityFieldDefn;
import io.github.jamoamo.webjourney.reserved.entity.EntityFromElementConverter;
import io.github.jamoamo.webjourney.annotation.Constant;
import io.github.jamoamo.webjourney.api.web.AElement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author James Amoore
 */
public class EntityFromElementConverterTest
{
	public static class Entity
	{
		private List<SubEntity> subEntities;

		public List<SubEntity> getSubEntities()
		{
			return subEntities;
		}

		public void setSubEntities(List<SubEntity> subEntities)
		{
			this.subEntities = subEntities;
		}
	}
	
	public static class SubEntity
	{
		@Constant("Value")
		private String field;

		public String getField()
		{
			return field;
		}

		public void setField(String field)
		{
			this.field = field;
		}
	}

	/**
	 * Test of convertValue method, of class EntityFromElementConverter.
	 */
	@Test
	public void testConvertValue() throws Exception
	{
		AElement element = Mockito.mock(AElement.class);
		Mockito.when(element.exists()).thenReturn(Boolean.TRUE);
		
		EntityFieldDefn fieldDefn = Mockito.mock(EntityFieldDefn.class);
		
		Answer<Class<?>> answer = (InvocationOnMock iom) -> SubEntity.class;
		
		Mockito.when(fieldDefn.getFieldType()).then(answer);
		
		Mockito.when(fieldDefn.getField()).thenReturn(Entity.class.getDeclaredField("subEntities"));
		
		IValueReader reader = Mockito.mock(IValueReader.class);
		
		EntityFromElementConverter converter = new EntityFromElementConverter(fieldDefn);
		Object convertValue = converter.convertValue(element, reader, new ArrayList<>());
		
		assertInstanceOf(SubEntity.class, convertValue);
		assertEquals("Value", ((SubEntity)convertValue).getField());
	}
	
}

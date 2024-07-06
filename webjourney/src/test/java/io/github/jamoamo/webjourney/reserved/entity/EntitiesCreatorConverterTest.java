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
import io.github.jamoamo.webjourney.reserved.entity.EntitiesCreatorConverter;
import io.github.jamoamo.webjourney.reserved.entity.EntityFieldDefn;
import io.github.jamoamo.webjourney.annotation.Constant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class EntitiesCreatorConverterTest
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
	 * Test of convertValue method, of class EntitiesCreatorConverter.
	 */
	@Test
	public void testConvertValue() throws Exception
	{
		EntityFieldDefn fieldDefn = Mockito.mock(EntityFieldDefn.class);
		Mockito.when(fieldDefn.getField()).thenReturn(Entity.class.getDeclaredField("subEntities"));
		
		IValueReader reader = Mockito.mock(IValueReader.class);
		Mockito.when(reader.getElementText("https://some.url", false)).thenReturn("Value");
		
		EntitiesCreatorConverter converter = new EntitiesCreatorConverter(fieldDefn);
		List<Object> convertValue = 
			converter.convertValue(Collections.singletonList("https://some.url"), reader, new ArrayList<>());
		
		assertEquals(1, convertValue.size());
		assertInstanceOf(SubEntity.class, convertValue.get(0));
		assertEquals("Value", ((SubEntity)convertValue.get(0)).getField());
	}
	
}

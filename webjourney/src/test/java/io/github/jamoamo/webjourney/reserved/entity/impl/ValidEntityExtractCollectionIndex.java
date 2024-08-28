/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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

import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreatorTest;
import java.util.List;
import io.github.jamoamo.webjourney.annotation.ExtractCollectionIndex;

/**
 *
 * @author James Amoore
 */
public class ValidEntityExtractCollectionIndex
{
	public static class SubEntity
	{
		@ExtractCollectionIndex(zeroBased = true)
		private Integer index;
		@ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA)
		private String stringData;
		@ExtractValue(path = EntityCreatorTest.XPATH_INT_DATA)
		private int intData;
		@ExtractValue(path = EntityCreatorTest.XPATH_DOUBLE_DATA)
		private double doubleData;

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}

		public String getStringData()
		{
			return stringData;
		}

		public void setStringData(String stringData)
		{
			this.stringData = stringData;
		}

		public int getIntData()
		{
			return intData;
		}

		public void setIntData(int intData)
		{
			this.intData = intData;
		}

		public double getDoubleData()
		{
			return doubleData;
		}

		public void setDoubleData(double doubleData)
		{
			this.doubleData = doubleData;
		}
	}
	
	@ExtractValue(path = EntityCreatorTest.XPATH_SUB_LIST_DATA)
	private List<SubEntity> subEntities;
	
	public List<SubEntity> getSubEntities()
	{
		return this.subEntities;
	}
	
	public void setSubEntities(List<SubEntity> subEntities)
	{
		this.subEntities = subEntities;
	}
}
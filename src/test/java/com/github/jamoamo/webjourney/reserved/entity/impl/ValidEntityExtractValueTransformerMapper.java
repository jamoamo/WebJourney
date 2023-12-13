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
package com.github.jamoamo.webjourney.reserved.entity.impl;

import com.github.jamoamo.webjourney.annotation.ExtractValue;
import com.github.jamoamo.webjourney.annotation.MappedCollection;
import com.github.jamoamo.webjourney.annotation.Transformation;
import static com.github.jamoamo.webjourney.reserved.entity.EntityCreatorTest.XPATH_SEPARATED_STRING_DATA;
import static com.github.jamoamo.webjourney.reserved.entity.EntityCreatorTest.XPATH_STRING_DATA;
import static com.github.jamoamo.webjourney.reserved.entity.EntityCreatorTest.XPATH_STRING_LIST_DATA;
import java.util.List;
import com.github.jamoamo.webjourney.annotation.Conversion;

/**
 *
 * @author James Amoore
 */
public class ValidEntityExtractValueTransformerMapper
{
	@ExtractValue(path = XPATH_STRING_DATA)
	@Transformation(transformFunction = TestTransformer.class)
	@Conversion(mapper = TestMapper.class)
	private String stringData;
	
	@ExtractValue(path = XPATH_SEPARATED_STRING_DATA)
	@Transformation(transformFunction = TestTransformer.class)
	@Conversion(mapper = StringSplitMapper.class)
	@MappedCollection()
	private List<String> stringListData;

	public String getStringData()
	{
		return stringData;
	}

	public void setStringData(String stringData)
	{
		this.stringData = stringData;
	}

	public List<String> getStringListData()
	{
		return stringListData;
	}

	public void setStringListData(List<String> stringListData)
	{
		this.stringListData = stringListData;
	}
	
	
}

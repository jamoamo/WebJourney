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
package com.github.jamoamo.webjourney.reserved.entity;

import com.github.jamoamo.webjourney.annotation.ConditionalExtractValue;
import com.github.jamoamo.webjourney.annotation.ExtractValue;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public class ValidRegexMatchConditionalExtractValue
{
	@ConditionalExtractValue.RegexMatch(
		ifExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA, attribute = "attr"),
		regexPattern = "Attribute.*",
		thenExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA)
	)
	private String singleCondition;
	
	@ConditionalExtractValue.RegexMatch(
		ifExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA, attribute = "attr"),
		regexPattern = "String.*",
		thenExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_INT_DATA)
	)
	@ConditionalExtractValue.RegexMatch(
		ifExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA, attribute = "attr"),
		regexPattern = "Attribute.*",
		thenExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA)
	)
	private String multipleConditions;
	
	@ConditionalExtractValue.RegexMatch(
		ifExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA, attribute = "attr"),
		regexPattern = "String.*",
		thenExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_INT_DATA)
	)
	@ConditionalExtractValue.RegexMatch(
		ifExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA, attribute = "attr"),
		regexPattern = "Attribute.*",
		thenExtractValue = @ExtractValue(path = EntityCreatorTest.XPATH_STRING_LIST_DATA)
	)
	private List<String> collection;

	public String getSingleCondition()
	{
		return singleCondition;
	}

	public void setSingleCondition(String singleCondition)
	{
		this.singleCondition = singleCondition;
	}

	public String getMultipleConditions()
	{
		return multipleConditions;
	}

	public void setMultipleConditions(String multipleConditions)
	{
		this.multipleConditions = multipleConditions;
	}

	public List<String> getCollection()
	{
		return collection;
	}

	public void setCollection(List<String> collection)
	{
		this.collection = collection;
	}
	
	
}

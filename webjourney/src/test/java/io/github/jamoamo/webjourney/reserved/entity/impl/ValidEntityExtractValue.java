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
package io.github.jamoamo.webjourney.reserved.entity.impl;

import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.annotation.RegexExtractValue;
import io.github.jamoamo.webjourney.reserved.entity.EntityCreatorTest;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public class ValidEntityExtractValue
{
	 private String noAnnotation;
	 @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA)
	 private String stringData;
	 @RegexExtractValue(extractValue =
		  @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA),
		  regexes =
		  {
				"(?<group>\\w+)\\sData"
		  }, groupName = "group")
	 private String regexStringData;

	 @ExtractValue(path = EntityCreatorTest.XPATH_INT_DATA)
	 private int intData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_DOUBLE_DATA)
	 private double doubleData;
	 @RegexExtractValue(extractValue =
		  @ExtractValue(path = EntityCreatorTest.XPATH_DOUBLE_DATA),
		  regexes =
		  {
				"(?<group>\\d+)(.\\d+)?"
		  }, groupName = "group", defaultValue = "0")
	 private Integer regexIntegerData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_SUB_DATA)
	 private SubEntity subData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_STRING_LIST_DATA)
	 private List<String> stringListData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_INTEGER_LIST_DATA)
	 private List<Integer> integerListData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_DOUBLE_LIST_DATA)
	 private List<Double> doubleListData;
	 @RegexExtractValue(extractValue =
		  @ExtractValue(path = EntityCreatorTest.XPATH_STRING_LIST_DATA),
		  regexes =
		  {
				"Item(?<group>\\d+)"
		  }, groupName = "group")
	 private List<Integer> regexIntegerListData;
	 @ExtractValue(path = EntityCreatorTest.XPATH_SUB_LIST_DATA)
	 private List<SubEntity> subs;

	 public static class SubEntity
	 {
		  @ExtractValue(path = EntityCreatorTest.XPATH_STRING_DATA)
		  private String stringData;
		  @ExtractValue(path = EntityCreatorTest.XPATH_INT_DATA)
		  private int intData;
		  @ExtractValue(path = EntityCreatorTest.XPATH_DOUBLE_DATA)
		  private double doubleData;

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

	 public String getNoAnnotation()
	 {
		  return noAnnotation;
	 }

	 public void setNoAnnotation(String noAnnotation)
	 {
		  this.noAnnotation = noAnnotation;
	 }

	 public String getRegexStringData()
	 {
		  return regexStringData;
	 }

	 public void setRegexStringData(String regexStringData)
	 {
		  this.regexStringData = regexStringData;
	 }

	 public Integer getRegexIntegerData()
	 {
		  return regexIntegerData;
	 }

	 public void setRegexIntegerData(Integer regexIntegerData)
	 {
		  this.regexIntegerData = regexIntegerData;
	 }

	 public List<Integer> getRegexIntegerListData()
	 {
		  return regexIntegerListData;
	 }

	 public void setRegexIntegerListData(List<Integer> regexIntegerListData)
	 {
		  this.regexIntegerListData = regexIntegerListData;
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

	 public SubEntity getSubData()
	 {
		  return subData;
	 }

	 public void setSubData(SubEntity subData)
	 {
		  this.subData = subData;
	 }

	 public List<String> getStringListData()
	 {
		  return stringListData;
	 }

	 public void setStringListData(List<String> stringListData)
	 {
		  this.stringListData = stringListData;
	 }

	 public List<Integer> getIntegerListData()
	 {
		  return integerListData;
	 }

	 public void setIntegerListData(List<Integer> integerListData)
	 {
		  this.integerListData = integerListData;
	 }

	 public List<Double> getDoubleListData()
	 {
		  return doubleListData;
	 }

	 public void setDoubleListData(List<Double> doubleListData)
	 {
		  this.doubleListData = doubleListData;
	 }

	 public List<SubEntity> getSubs()
	 {
		  return subs;
	 }

	 public void setSubs(List<SubEntity> subs)
	 {
		  this.subs = subs;
	 }

}

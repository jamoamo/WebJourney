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

import com.github.jamoamo.webjourney.api.web.AElement;
import com.github.jamoamo.webjourney.api.web.IBrowser;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 *
 * @author James Amoore
 */
public class EntityCreatorTest
{
	public static final String XPATH_STRING_DATA = "//div[@id='string-data']";
	public static final String XPATH_DIFF_STRING_DATA = "//div[@id='string-diff-data']";
	public static final String XPATH_INT_DATA = "//div[@id='int-data']";
	public static final String XPATH_DOUBLE_DATA = "//div[@id='double-data']";
	public static final String XPATH_SUB_DATA = "//div[@id='sub-data']";
	public static final String XPATH_URL_DATA = "//div[@id='url-data']";
	public static final String XPATH_STRING_LIST_DATA = "//div[@id='string-list-data']";
	public static final String XPATH_INTEGER_LIST_DATA = "//div[@id='integer-list-data']";
	public static final String XPATH_DOUBLE_LIST_DATA = "//div[@id='double-list-data']";
	public static final String XPATH_SUB_LIST_DATA = "//div[@id='sub-list-data']";
	public static final String XPATH_URL_LIST_DATA = "//div[@class='url-list-data']";
	public static final String XPATH_SEPARATED_STRING_DATA = "//div[@id='split-string-data']";
	
	private static IBrowser browser;
	
	@BeforeAll
	public static void setup()
	{
		browser = mock(IBrowser.class);
		
		when(browser.getElementText(XPATH_STRING_DATA)).thenReturn("String Data");
		when(browser.getElement(XPATH_STRING_DATA)).thenReturn(
				  new TestElement("div", Collections.singletonMap("attr", "Attribute Value"), "String Data"));
		
		when(browser.getElementText(XPATH_INT_DATA)).thenReturn("57");
		when(browser.getElement(XPATH_INT_DATA)).thenReturn(new TestElement("57"));
		
		when(browser.getElementText(XPATH_DOUBLE_DATA)).thenReturn("125.9");
		when(browser.getElement(XPATH_DOUBLE_DATA)).thenReturn(new TestElement("125.9"));
		
		when(browser.getElement(XPATH_SUB_DATA)).thenReturn(
				  new TestElement(
							 "",
							 new AElement[]{
								 new TestElement("div", Collections.singletonMap("id", "string-data"), "String Data"),
								 new TestElement("div", Collections.singletonMap("id", "int-data"), "57"),
								 new TestElement("div", Collections.singletonMap("id", "double-data"), "125.9")
							 }
				  )
		);
		
		when(browser.getElementText(XPATH_URL_DATA)).thenReturn("https://newurl.com");
		when(browser.getElement(XPATH_URL_DATA)).thenReturn(new TestElement("a", Collections.singletonMap("href", "https://newurlattr.com") ,"https://newurl.com"));
		
		List<TestElement> testElements = new ArrayList<>();
		testElements.add(new TestElement("Item1"));
		testElements.add(new TestElement("Item2"));
		testElements.add(new TestElement("Item3"));
		
		Answer<List<TestElement>> answer = new Answer<List<TestElement>>()
		{
			@Override
			public List<TestElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return testElements;
			}
		};
		
		Answer<List<String>> stringAnswer = new Answer<List<String>>()
		{
			@Override
			public List<String> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return testElements.stream().map(elem -> elem.getElementText()).toList();
			}
		};
		
		when(browser.getElements(XPATH_STRING_LIST_DATA)).thenAnswer(answer);
		when(browser.getElementTexts(XPATH_STRING_LIST_DATA)).thenAnswer(stringAnswer);
		
		List<TestElement> intElements = new ArrayList<>();
		intElements.add(new TestElement("1"));
		intElements.add(new TestElement("2"));
		intElements.add(new TestElement("3"));
		
		Answer<List<TestElement>> intAnswer = new Answer<List<TestElement>>()
		{
			@Override
			public List<TestElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return intElements;
			}
		};
		
		Answer<List<String>> intStringAnswer = new Answer<List<String>>()
		{
			@Override
			public List<String> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return intElements.stream().map(elem -> elem.getElementText()).toList();
			}
		};
		
		when(browser.getElements(XPATH_INTEGER_LIST_DATA)).thenAnswer(intAnswer);
		when(browser.getElementTexts(XPATH_INTEGER_LIST_DATA)).thenAnswer(intStringAnswer);
		
		List<TestElement> doubleElements = new ArrayList<>();
		doubleElements.add(new TestElement("1.1"));
		doubleElements.add(new TestElement("2.2"));
		doubleElements.add(new TestElement("3.3"));
		
		Answer<List<TestElement>> doubleAnswer = new Answer<List<TestElement>>()
		{
			@Override
			public List<TestElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return doubleElements;
			}
		};
		
		Answer<List<String>> doubleStringAnswer = new Answer<List<String>>()
		{
			@Override
			public List<String> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return doubleElements.stream().map(elem -> elem.getElementText()).toList();
			}
		};
		
		when(browser.getElements(XPATH_DOUBLE_LIST_DATA)).thenAnswer(doubleAnswer);
		when(browser.getElementTexts(XPATH_DOUBLE_LIST_DATA)).thenAnswer(doubleStringAnswer);
		
		List<TestElement> subElements = new ArrayList<>();
		subElements.add(new TestElement(
							 "",
							 new AElement[]{
								 new TestElement("div", Collections.singletonMap("id", "string-data"), "String Data 1"),
								 new TestElement("div", Collections.singletonMap("id", "int-data"), "571"),
								 new TestElement("div", Collections.singletonMap("id", "double-data"), "125.91")
							 }
				  ));
		subElements.add(new TestElement(
							 "",
							 new AElement[]{
								 new TestElement("div", Collections.singletonMap("id", "string-data"), "String Data 2"),
								 new TestElement("div", Collections.singletonMap("id", "int-data"), "572"),
								 new TestElement("div", Collections.singletonMap("id", "double-data"), "125.92")
							 }
				  ));
		subElements.add(new TestElement(
							 "",
							 new AElement[]{
								 new TestElement("div", Collections.singletonMap("id", "string-data"), "String Data 3"),
								 new TestElement("div", Collections.singletonMap("id", "int-data"), "573"),
								 new TestElement("div", Collections.singletonMap("id", "double-data"), "125.93")
							 }
				  ));
		
		Answer<List<TestElement>> subAnswer = new Answer<List<TestElement>>()
		{
			@Override
			public List<TestElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return subElements;
			}
		};
		
		when(browser.getElements(XPATH_SUB_LIST_DATA)).thenAnswer(subAnswer);
		
		List<TestElement> urlElements = new ArrayList<>();
		urlElements.add(new TestElement("a", Collections.singletonMap("href", "https://newurlattr1.com") 
			,"https://newurl1.com"));
		urlElements.add(new TestElement("a", Collections.singletonMap("href", "https://newurlattr2.com") 
			,"https://newurl2.com"));
		urlElements.add(new TestElement("a", Collections.singletonMap("href", "https://newurlattr3.com") 
			,"https://newurl3.com"));
		
		Answer<List<TestElement>> urlAnswer = new Answer<List<TestElement>>()
		{
			@Override
			public List<TestElement> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return urlElements;
			}
		};
		
		Answer<List<String>> urlStringAnswer = new Answer<List<String>>()
		{
			@Override
			public List<String> answer(InvocationOnMock iom)
					  throws Throwable
			{
				return urlElements.stream().map(elem -> elem.getElementText()).toList();
			}
		};
		
		when(browser.getElements(XPATH_URL_LIST_DATA)).thenAnswer(urlAnswer);
		when(browser.getElementTexts(XPATH_URL_LIST_DATA)).thenAnswer(urlStringAnswer);
		
		when(browser.getElement(XPATH_SEPARATED_STRING_DATA)).thenReturn(new TestElement("item1,item2,item3"));
		when(browser.getElementText(XPATH_SEPARATED_STRING_DATA)).thenReturn("item1,item2,item3");
		when(browser.getCurrentUrl()).thenReturn("https://currenturl.com");
		
		when(browser.getElement(XPATH_DIFF_STRING_DATA)).thenReturn(new TestElement("String1"), new TestElement("String2"), new TestElement("String3"), new TestElement("String4"), new TestElement("String5"));
		when(browser.getElementText(XPATH_DIFF_STRING_DATA)).thenReturn("String1", "String2", "String3", "String4", "String5");
	}
	
	/**
	 * Test of createNewEntity method, of class EntityCreator.
	 */
	@Test
	public void testCreateNewEntity_ExtractValue_noMapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValue.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValue createNewEntity = (ValidEntityExtractValue)creator.createNewEntity(browser);
		assertNull(createNewEntity.getNoAnnotation());
		assertEquals("String Data", createNewEntity.getStringData());
		assertEquals("String", createNewEntity.getRegexStringData());
		assertEquals(57, createNewEntity.getIntData());
		assertEquals(125, createNewEntity.getRegexIntegerData());
		assertEquals(125.9, createNewEntity.getDoubleData());
		assertNotNull(createNewEntity.getSubData());
		assertEquals("String Data", createNewEntity.getSubData().getStringData());
		assertEquals(57, createNewEntity.getSubData().getIntData());
		assertEquals(125.9, createNewEntity.getSubData().getDoubleData());
		assertEquals(3, createNewEntity.getStringListData().size());
		assertEquals("Item1", createNewEntity.getStringListData().get(0));
		assertEquals("Item2", createNewEntity.getStringListData().get(1));
		assertEquals("Item3", createNewEntity.getStringListData().get(2));
		assertEquals(3, createNewEntity.getRegexIntegerListData().size());
		assertEquals(1, createNewEntity.getRegexIntegerListData().get(0));
		assertEquals(2, createNewEntity.getRegexIntegerListData().get(1));
		assertEquals(3, createNewEntity.getRegexIntegerListData().get(2));
		assertEquals(3, createNewEntity.getDoubleListData().size());
		assertEquals(1.1, createNewEntity.getDoubleListData().get(0).doubleValue());
		assertEquals(2.2, createNewEntity.getDoubleListData().get(1).doubleValue());
		assertEquals(3.3, createNewEntity.getDoubleListData().get(2).doubleValue());
		assertEquals(3, createNewEntity.getIntegerListData().size());
		assertEquals(1, createNewEntity.getIntegerListData().get(0).doubleValue());
		assertEquals(2, createNewEntity.getIntegerListData().get(1).doubleValue());
		assertEquals(3, createNewEntity.getIntegerListData().get(2).doubleValue());
		assertEquals(3, createNewEntity.getSubs().size());
		assertNotNull(createNewEntity.getSubs().get(0));
		assertEquals("String Data 1", createNewEntity.getSubs().get(0).getStringData());
		assertEquals(571, createNewEntity.getSubs().get(0).getIntData());
		assertEquals(125.91, createNewEntity.getSubs().get(0).getDoubleData());
		assertNotNull(createNewEntity.getSubs().get(1));
		assertEquals("String Data 2", createNewEntity.getSubs().get(1).getStringData());
		assertEquals(572, createNewEntity.getSubs().get(1).getIntData());
		assertEquals(125.92, createNewEntity.getSubs().get(1).getDoubleData());
		assertNotNull(createNewEntity.getSubs().get(2));
		assertEquals("String Data 3", createNewEntity.getSubs().get(2).getStringData());
		assertEquals(573, createNewEntity.getSubs().get(2).getIntData());
		assertEquals(125.93, createNewEntity.getSubs().get(2).getDoubleData());

	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Mapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueMapper createNewEntity = (ValidEntityExtractValueMapper)creator.createNewEntity(browser);
		assertEquals("<String Data>", createNewEntity.getStringData());
		assertEquals(3, createNewEntity.getWrappers().size());
		assertEquals("Item1", createNewEntity.getWrappers().get(0).getValue());
		assertEquals("Item2", createNewEntity.getWrappers().get(1).getValue());
		assertEquals("Item3", createNewEntity.getWrappers().get(2).getValue());
		assertEquals(3, createNewEntity.getSplitStrings().size());
		assertEquals("item1", createNewEntity.getSplitStrings().get(0));
		assertEquals("item2", createNewEntity.getSplitStrings().get(1));
		assertEquals("item3", createNewEntity.getSplitStrings().get(2));
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_noMapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueTransformer.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueTransformer createNewEntity = (ValidEntityExtractValueTransformer)creator.createNewEntity(browser);
		assertEquals("ataD gnirtS", createNewEntity.getStringData());
		assertEquals("gnirtS", createNewEntity.getRegexStringData());
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Mapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueTransformerMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueTransformerMapper createNewEntity = (ValidEntityExtractValueTransformerMapper)creator.createNewEntity(browser);
		assertEquals("<ataD gnirtS>", createNewEntity.getStringData());
		assertEquals(3, createNewEntity.getStringListData().size());
		assertEquals("3meti", createNewEntity.getStringListData().get(0));
		assertEquals("2meti", createNewEntity.getStringListData().get(1));
		assertEquals("1meti", createNewEntity.getStringListData().get(2));
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Attribute_noMapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueAttribute.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueAttribute createNewEntity = (ValidEntityExtractValueAttribute)creator.createNewEntity(browser);
		assertEquals("Attribute Value", createNewEntity.getAttribute());
		assertEquals("Attribute", createNewEntity.getRegexAttribute());
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Attribute_Mapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueAttributeMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueAttributeMapper createNewEntity = (ValidEntityExtractValueAttributeMapper)creator.createNewEntity(browser);
		assertEquals("<Attribute Value>", createNewEntity.getAttribute());
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Attribute_noMapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueAttributeTransformer.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueAttributeTransformer createNewEntity = (ValidEntityExtractValueAttributeTransformer)creator.createNewEntity(browser);
		assertEquals("eulaV etubirttA", createNewEntity.getAttribute());
	}
	
	@Test
	public void testCreateNewEntity_ExtractValue_Attribute_Mapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractValueAttributeTransformerMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		ValidEntityExtractValueAttributeTransformerMapper createNewEntity = (ValidEntityExtractValueAttributeTransformerMapper)creator.createNewEntity(browser);
		assertEquals("<eulaV etubirttA>", createNewEntity.getAttribute());
	}
	
	@Test
	public void testCreateNewEntity_ExtractCurrentUrl_noMapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractCurrentUrl.class);
		EntityCreator creator = new EntityCreator(defn);

		
		ValidEntityExtractCurrentUrl createNewEntity = (ValidEntityExtractCurrentUrl)creator.createNewEntity(browser);
		assertEquals("https://currenturl.com", createNewEntity.getCurrentUrl());
	}
	
	@Test
	public void testCreateNewEntity_ExtractCurrentUrl_Mapper_noTransformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractCurrentUrlMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		
		ValidEntityExtractCurrentUrlMapper createNewEntity = (ValidEntityExtractCurrentUrlMapper)creator.createNewEntity(browser);
		assertEquals("<https://currenturl.com>", createNewEntity.getCurrentUrl());
	}
	
	@Test
	public void testCreateNewEntity_ExtractCurrentUrl_noMapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractCurrentUrlTransformer.class);
		EntityCreator creator = new EntityCreator(defn);

		
		ValidEntityExtractCurrentUrlTransformer createNewEntity = (ValidEntityExtractCurrentUrlTransformer)creator.createNewEntity(browser);
		assertEquals("moc.lrutnerruc//:sptth", createNewEntity.getCurrentUrl());
	}
	
	@Test
	public void testCreateNewEntity_ExtractCurrentUrl_Mapper_Transformation()
	{
		EntityDefn defn = new EntityDefn(ValidEntityExtractCurrentUrlTransformerMapper.class);
		EntityCreator creator = new EntityCreator(defn);

		
		ValidEntityExtractCurrentUrlTransformerMapper createNewEntity = (ValidEntityExtractCurrentUrlTransformerMapper)creator.createNewEntity(browser);
		assertEquals("<moc.lrutnerruc//:sptth>", createNewEntity.getCurrentUrl());
	}
	
	@Test
	public void testCreateNewEntity_ExtractFromUrl()
	{
		Mockito.clearInvocations(browser);
		EntityDefn defn = new EntityDefn(ValidEntityExtractFromUrl.class);
		EntityCreator creator = new EntityCreator(defn);
		
		ValidEntityExtractFromUrl createNewEntity = (ValidEntityExtractFromUrl)creator.createNewEntity(browser);
		
		Mockito.verify(browser).navigateBack();
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(browser).navigateToUrl(urlCaptor.capture());
		assertEquals("https://newurl.com", urlCaptor.getValue().toString());
		
		assertNotNull(createNewEntity.getUrlEntity());
		assertEquals("<ataD gnirtS>", createNewEntity.getUrlEntity().getStringData());
	}
	
	@Test
	public void testCreateNewEntity_ExtractFromUrl_Attribute()
	{
		Mockito.clearInvocations(browser);
		EntityDefn defn = new EntityDefn(ValidEntityExtractFromUrlAttribute.class);
		EntityCreator creator = new EntityCreator(defn);
		
		ValidEntityExtractFromUrlAttribute createNewEntity = (ValidEntityExtractFromUrlAttribute)creator.createNewEntity(browser);
		
		Mockito.verify(browser).navigateBack();
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(browser).navigateToUrl(urlCaptor.capture());
		assertEquals("https://newurlattr.com", urlCaptor.getValue().toString());
		
		assertNotNull(createNewEntity.getUrlEntity());
		assertEquals("<ataD gnirtS>", createNewEntity.getUrlEntity().getStringData());
	}
	
	@Test
	public void testCreateNewEntity_ConditionalExtractValue_RegexMatch()
	{
		Mockito.clearInvocations(browser);
		EntityDefn defn = new EntityDefn(ValidRegexMatchConditionalExtractValue.class);
		EntityCreator creator = new EntityCreator(defn);
		
		ValidRegexMatchConditionalExtractValue createNewEntity = (ValidRegexMatchConditionalExtractValue)creator.createNewEntity(browser);
		
		assertEquals("String Data", createNewEntity.getSingleCondition());
		assertEquals("String Data", createNewEntity.getMultipleConditions());
		assertEquals(3, createNewEntity.getCollection().size());
		assertEquals("Item1", createNewEntity.getCollection().get(0));
		assertEquals("Item2", createNewEntity.getCollection().get(1));
		assertEquals("Item3", createNewEntity.getCollection().get(2));
	}
	
	@Test
	public void testCreateNewEntity_ConditionalExtractFromUrl_RegexMatch()
	{
		Mockito.clearInvocations(browser);
		EntityDefn defn = new EntityDefn(ValidRegexMatchConditionalExtractFromUrl.class);
		EntityCreator creator = new EntityCreator(defn);
		
		ValidRegexMatchConditionalExtractFromUrl createNewEntity = (ValidRegexMatchConditionalExtractFromUrl)creator.createNewEntity(browser);
		
		Mockito.verify(browser, times(5)).navigateBack();
		ArgumentCaptor<URL> urlCaptor = ArgumentCaptor.forClass(URL.class);
		Mockito.verify(browser, times(5)).navigateToUrl(urlCaptor.capture());
		assertEquals("https://newurlattr.com", urlCaptor.getAllValues().get(0).toString());
		assertEquals("https://newurl.com", urlCaptor.getAllValues().get(1).toString());
		assertEquals("https://newurlattr1.com", urlCaptor.getAllValues().get(2).toString());
		assertEquals("https://newurlattr2.com", urlCaptor.getAllValues().get(3).toString());
		assertEquals("https://newurlattr3.com", urlCaptor.getAllValues().get(4).toString());
		
		assertNotNull(createNewEntity.getSingleCondition());
		assertEquals("String1", createNewEntity.getSingleCondition().getStringData());
		assertNotNull(createNewEntity.getMultipleConditions());
		assertEquals("String2", createNewEntity.getMultipleConditions().getStringData());
		assertEquals(3, createNewEntity.getCollection().size());
		assertEquals("String3", createNewEntity.getCollection().get(0).getStringData());
		assertEquals("String4", createNewEntity.getCollection().get(1).getStringData());
		assertEquals("String5", createNewEntity.getCollection().get(2).getStringData());
	}
}

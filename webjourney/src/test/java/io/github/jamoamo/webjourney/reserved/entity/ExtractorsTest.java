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

import io.github.jamoamo.webjourney.annotation.ConditionalConstant;
import io.github.jamoamo.webjourney.annotation.ConditionalExtractFromUrl;
import io.github.jamoamo.webjourney.annotation.ConditionalExtractValue;
import io.github.jamoamo.webjourney.annotation.Constant;
import io.github.jamoamo.webjourney.annotation.ExtractCurrentUrl;
import io.github.jamoamo.webjourney.annotation.ExtractFromUrl;
import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.annotation.RegexExtractCurrentUrl;
import io.github.jamoamo.webjourney.annotation.RegexExtractValue;
import io.github.jamoamo.webjourney.reserved.reflection.FieldInfo;
import io.github.jamoamo.webjourney.reserved.reflection.TypeInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class ExtractorsTest
{
	public static class Entity
	{
		private String stringField;

		public String getStringField()
		{
			return stringField;
		}

		public void setStringField(String stringField)
		{
			this.stringField = stringField;
		}
	}
	
	
	/**
	 * Test of getExtractorForAnnotation method, of class Extractors.
	 */
	@Test
	public void testGetExtractorForAnnotation_Constant()
	{
		Field field = Mockito.mock(Field.class);
		
		Constant constant = new Constant()
		{
			@Override
			public String value()
			{
				return "Constant Value";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return Constant.class;
			}
		};
		
		FieldInfo fieldInfo = FieldInfo.forField(field);
		IExtractor extractor = Extractors.getExtractorForAnnotation(constant, fieldInfo, false, false);
		assertInstanceOf(ConstantExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractCurrentUrl()
	{
		Field field = Mockito.mock(Field.class);
		
		ExtractCurrentUrl extractCurrentUrl = new ExtractCurrentUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractCurrentUrl.class;
			}
		};
		
		FieldInfo fieldInfo = FieldInfo.forField(field);
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractCurrentUrl, fieldInfo, false, false);
		assertInstanceOf(CurrentUrlExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_Collection_BlankAttribute()
	{
		Field field = Mockito.mock(Field.class);
		
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "";
			}
			
			@Override
			public boolean optional()
			{
				 return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementTextsCollectionExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_Collection_Attribute()
	{
		Field field = Mockito.mock(Field.class);
		
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "attr";
			}

			@Override
			public boolean optional()
			{
				 return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(AttributesExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_Attribute()
	{
		Field field = Mockito.mock(Field.class);
		
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "attr";
			}
			
			@Override
			public boolean optional()
			{
				 return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(AttributeExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_Attribute_OptionalElement()
	{
		Field field = Mockito.mock(Field.class);
		
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "attr";
			}
			
			@Override
			public boolean optional()
			{
				 return true;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(AttributeExtractor.class, extractor);
		assertTrue(((AttributeExtractor)extractor).getOptional());
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_BlankAttribute()
	{
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "";
			}
			
			@Override
			public boolean optional()
			{
				 return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractFromUrl_BlankAttribute_optional()
	{
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}

			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "";
			}
			
			@Override
			public boolean optional()
			{
				 return true;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
		assertTrue(((ElementTextExtractor)extractor).isOptional());
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_Attribute()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "attr";
			}

			@Override
			public String path()
			{
				return "xpath";
			}

			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(AttributeExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_BlankAttribute_extractCollectionSingularly()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, true, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_BlankAttribute_extractCollectionSingularly_optional()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return true;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, true, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
		 ElementTextExtractor textExtractor = (ElementTextExtractor)extractor;
		 assertTrue(textExtractor.isOptional());
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_BlankAttribute_noExtractCollectionSingularly()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementTextsCollectionExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_BlankAttribute_noExtractCollectionSingularly_hasConverter()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, true);
		assertInstanceOf(ElementTextsCollectionExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_BlankAttribute_noExtractCollectionSingularly_noConverter()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementListExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_List_Attribute()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "attr";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(List.class));
		Mockito.when(fieldInfo.getFieldGenericTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(AttributesExtractor.class, extractor);
	}
		
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_Entity_hasConverter()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, true);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_Entity_noConverter()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ExtractValue_String()
	{
		ExtractValue extractFromUrl = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(extractFromUrl, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_RegexExtractValue_Attribute()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "attr";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		RegexExtractValue regex = new RegexExtractValue()
		{
			@Override
			public ExtractValue extractValue()
			{
				return extractValue;
			}

			@Override
			public String[] regexes()
			{
				return new String[]{"regex"};
			}

			@Override
			public String groupName()
			{
				return "group";
			}

			@Override
			public String defaultValue()
			{
				return "";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return RegexExtractValue.class;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(AttributeExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_RegexExtractValue_BlankAttribute()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		RegexExtractValue regex = new RegexExtractValue()
		{
			@Override
			public ExtractValue extractValue()
			{
				return extractValue;
			}

			@Override
			public String[] regexes()
			{
				return new String[]{"regex"};
			}

			@Override
			public String groupName()
			{
				return "group";
			}

			@Override
			public String defaultValue()
			{
				return "";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return RegexExtractValue.class;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ConditionalExtractValue_RegexMatch_BlankAttribute()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		ConditionalExtractValue.RegexMatch regex = new ConditionalExtractValue.RegexMatch()
		{
			@Override
			public ExtractValue ifExtractValue()
			{
				return extractValue;
			}

			@Override
			public ExtractValue thenExtractValue()
			{
				return extractValue;
			}

			@Override
			public String regexPattern()
			{
				return "regex";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ConditionalExtractValue.RegexMatch.class;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ConditionalExtractValue_RegexMatch_Attribute()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "attr";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		ConditionalExtractValue.RegexMatch regex = new ConditionalExtractValue.RegexMatch()
		{
			@Override
			public ExtractValue ifExtractValue()
			{
				return extractValue;
			}

			@Override
			public ExtractValue thenExtractValue()
			{
				return extractValue;
			}

			@Override
			public String regexPattern()
			{
				return "regex";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ConditionalExtractValue.RegexMatch.class;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(AttributeExtractor.class, extractor);
	}
		
	@Test
	public void testGetExtractorForAnnotation_ConditionalExtractFromUrl_RegexMatch_BlankAttribute()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		ExtractFromUrl extractFromUrl = new ExtractFromUrl()
		{
			@Override
			public String urlXpath()
			{
				return "xpath";
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractFromUrl.class;
			}
			
			@Override
			public boolean optional()
			{
				 return false;
			}
		};
		
		ConditionalExtractFromUrl.RegexMatch regex = new ConditionalExtractFromUrl.RegexMatch()
		{
			@Override
			public ExtractValue ifExtractValue()
			{
				return extractValue;
			}
			
			@Override
			public ExtractFromUrl thenExtractFromUrl()
			{
				return extractFromUrl;
			}

			@Override
			public String regexPattern()
			{
				return "regex";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ConditionalExtractValue.RegexMatch.class;
			}
			
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(Entity.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(ElementTextExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_ConditionalConstant_RegexMatch()
	{
		ExtractValue extractValue = new ExtractValue()
		{
			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ExtractValue.class;
			}

			@Override
			public String attribute()
			{
				return "";
			}

			@Override
			public String path()
			{
				return "xpath";
			}
			
			@Override
			public boolean optional()
			{
				return false;
			}
		};
		
		Constant constant = new Constant()
		{
			@Override
			public String value()
			{
				return "Constant Value";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return Constant.class;
			}
		};
		
		ConditionalConstant.RegexMatch regex = new ConditionalConstant.RegexMatch()
		{
			@Override
			public ExtractValue ifExtractValue()
			{
				return extractValue;
			}

			@Override
			public Constant thenConstant()
			{
				return constant;
			}

			@Override
			public String regexPattern()
			{
				return "regex";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return ConditionalConstant.RegexMatch.class;
			}
			
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(ConstantExtractor.class, extractor);
	}
	
	@Test
	public void testGetExtractorForAnnotation_RegexExtractCurrentUrl()
	{
		RegexExtractCurrentUrl regex = new RegexExtractCurrentUrl()
		{

			@Override
			public String[] regexes()
			{
				return new String[]{"regex"};
			}

			@Override
			public String groupName()
			{
				return "group";
			}

			@Override
			public String defaultValue()
			{
				return "";
			}

			@Override
			public Class<? extends Annotation> annotationType()
			{
				return RegexExtractValue.class;
			}
		};
		
		FieldInfo fieldInfo = Mockito.mock(FieldInfo.class);
		Mockito.when(fieldInfo.getFieldTypeInfo()).thenReturn(TypeInfo.forClass(String.class));
		
		IExtractor extractor = Extractors.getExtractorForAnnotation(regex, fieldInfo, false, false);
		assertInstanceOf(CurrentUrlExtractor.class, extractor);
	}
}

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
package com.github.jamoamo.webjourney.api.mapper;

import java.time.LocalDate;
import java.time.Month;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter for LocalDate.
 * @author James Amoore
 */
public final class DateConverter extends AConverter<LocalDate>
{
	//Matches full date, e.g. 28th Feburary 2023
	private Pattern datePattern = 
		Pattern.compile("(?<day>\\d{1,2})(st|nd|rd|th)\\s(?<month>\\w+)\\s(?<year>\\d{4})");
	
	/**
	 * Converts a String to a LocalDate.
	 * @param value the value to convert
	 * @return the converted value
	 * @throws XValueMappingException if the string cannot be converted to a date.
	 */
	@Override
	public LocalDate mapValue(String value)
		throws XValueMappingException
	{
		Matcher dateMatcher = this.datePattern.matcher(value);
		if(dateMatcher.find())
		{
			String dayString = dateMatcher.group("day");
			String monthString = dateMatcher.group("month");
			String yearString = dateMatcher.group("year");
			
			return LocalDate.of(Integer.parseInt(yearString), 
									  Month.valueOf(monthString.toUpperCase()), 
									  Integer.parseInt(dayString));
		}
		throw new XValueMappingException("Unsupported date format");
	}
	
}

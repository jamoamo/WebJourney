/*
 * The MIT License
 *
 * Copyright 2022 James Amoore.
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
package com.github.jamoamo.entityscraper.api;

import com.github.jamoamo.entityscraper.annotation.Entity;
import com.github.jamoamo.entityscraper.annotation.XPath;

/**
 *
 * @author James Amoore
 */
@Entity
public class TestEntity
{
	@XPath (path = "/h1")
	private String title;
	@XPath (path = "/h2")
	private String subtitle;
	@XPath (path = "/table/tbody/tr[2]/td[1]")
	private String value1;
	@XPath (path = "/table/tbody/tr[2]/td[2]")
	private String value2;
	@XPath (path = "/table/tbody/tr[2]/td[3]")
	private int value3;
	@XPath (path = "/table/tbody/tr[2]/td[4]")
	private Double rate;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getSubtitle()
	{
		return subtitle;
	}

	public void setSubtitle(String subtitle)
	{
		this.subtitle = subtitle;
	}

	public String getValue1()
	{
		return value1;
	}

	public void setValue1(String value1)
	{
		this.value1 = value1;
	}

	public String getValue2()
	{
		return value2;
	}

	public void setValue2(String value2)
	{
		this.value2 = value2;
	}
	
	public int getValue3()
	{
		return this.value3;
	}
	
	public void setValue3(int value3)
	{
		this.value3 = value3;
	}
	
	public Double getRate()
	{
		return this.rate;
	}
	
	public void setRate(Double rate)
	{
		this.rate = rate;
	}
}

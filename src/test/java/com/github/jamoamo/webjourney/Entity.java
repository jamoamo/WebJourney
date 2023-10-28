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
package com.github.jamoamo.webjourney;

import com.github.jamoamo.webjourney.annotation.ExtractValue;
import java.util.List;

/**
 *
 * @author James Amoore
 */
public class Entity
{
	@ExtractValue(path = "//div[@id='columnLeft']/table/tbody/tr[2]/td[2]")
	private String testName;
	
	@ExtractValue(path = "//div[@id='columnLeft']/table[1]/tbody/tr[1]/td[2]/b[1]/font[1]/center[1]/a")
	private List<Team> teams;
	
	public static class Team
	{
		@ExtractValue(path = ".", attribute = "href")
		private String url;

		@ExtractValue(path = ".")
		private String name;

		public String getUrl()
		{
			return url;
		}

		public String getName()
		{
			return name;
		}

		public void setUrl(String url)
		{
			this.url = url;
		}

		public void setName(String name)
		{
			this.name = name;
		} 
	}
	
	public String getTestName()
	{
		return testName;
	}

	public void setTestName(String testName)
	{
		this.testName = testName;
	}

	public List<Team> getTeams()
	{
		return teams;
	}

	public void setTeams(List<Team> teams)
	{
		this.teams = teams;
	}
	
	
}

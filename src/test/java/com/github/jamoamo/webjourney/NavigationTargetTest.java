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

import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author James Amoore
 */
public class NavigationTargetTest
{
	
	public NavigationTargetTest()
	{
	}

	/**
	 * Test of toUrl method, of class NavigationTarget.
	 */
	@Test
	public void testToUrl_URL() throws Exception
	{
		ANavigationTarget target = NavigationTarget.toUrl(new URL("http://www.google.com"));
		Assertions.assertTrue(target instanceof UrlNavigationTarget);
	}

	/**
	 * Test of toUrl method, of class NavigationTarget.
	 */
	@Test
	public void testToUrl_String()
			  throws Exception
	{
		ANavigationTarget target = NavigationTarget.toUrl("http://www.google.com");
		Assertions.assertTrue(target instanceof UrlNavigationTarget);
	}

	/**
	 * Test of back method, of class NavigationTarget.
	 */
	@Test
	public void testBack()
	{
		ANavigationTarget target = NavigationTarget.back();
		Assertions.assertTrue(target instanceof BackNavigationTarget);
	}

	/**
	 * Test of forward method, of class NavigationTarget.
	 */
	@Test
	public void testForward()
	{
		ANavigationTarget target = NavigationTarget.forward();
		Assertions.assertTrue(target instanceof ForwardNavigationTarget);
	}

	/**
	 * Test of refresh method, of class NavigationTarget.
	 */
	@Test
	public void testRefresh()
	{
		ANavigationTarget target = NavigationTarget.refresh();
		Assertions.assertTrue(target instanceof RefreshNavigationTarget);
	}
	
}

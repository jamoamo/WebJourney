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

import com.github.jamoamo.webjourney.api.web.IBrowser;
import com.github.jamoamo.webjourney.api.web.IBrowserWindow;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author James Amoore
 */
public class RefreshNavigationTargetTest
{
	
	public RefreshNavigationTargetTest()
	{
	}

	/**
	 * Test of navigate method, of class RefreshNavigationTarget.
	 */
	@Test
	public void testNavigate() throws Exception
	{
		IBrowser browser = Mockito.mock(IBrowser.class);
		IBrowserWindow window = Mockito.mock(IBrowserWindow.class);
		
		Mockito.when(browser.getActiveWindow()).thenReturn(window);
		RefreshNavigationTarget target = new RefreshNavigationTarget();
		target.navigate(browser);
		Mockito.verify(window, Mockito.times(1)).refreshCurrentPage();
	}
	
}

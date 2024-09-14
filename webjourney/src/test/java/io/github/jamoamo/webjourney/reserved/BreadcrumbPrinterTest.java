/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
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
package io.github.jamoamo.webjourney.reserved;

import io.github.jamoamo.webjourney.api.ICrumb;
import io.github.jamoamo.webjourney.api.IJourneyBreadcrumb;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author James Amoore
 */
public class BreadcrumbPrinterTest
{
	
	public BreadcrumbPrinterTest()
	{
	}

	/**
	 * Test of printBreadCrumb method, of class BreadcrumbPrinter.
	 */
	@Test
	public void testPrintBreadCrumb_1crumb()
		 throws Exception
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IJourneyBreadcrumb breadcrumb = new JourneyBreadcrumb();
		
		ICrumb crumb1 = Mockito.mock(ICrumb.class);
		Mockito.when(crumb1.getCrumbName()).thenReturn("Crumb1");
		Mockito.when(crumb1.getCrumbType()).thenReturn("Type");
		breadcrumb.pushCrumb(crumb1);
		BreadcrumbPrinter instance = new BreadcrumbPrinter();
		instance.printBreadCrumb(stream, breadcrumb);
		
		assertEquals("(Type) Crumb1", stream.toString());
	}
	
	@Test
	public void testPrintBreadCrumb_2crumbs()
		 throws Exception
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IJourneyBreadcrumb breadcrumb = new JourneyBreadcrumb();
		
		ICrumb crumb1 = Mockito.mock(ICrumb.class);
		Mockito.when(crumb1.getCrumbName()).thenReturn("Crumb1");
		Mockito.when(crumb1.getCrumbType()).thenReturn("Type");
		
		ICrumb crumb2 = Mockito.mock(ICrumb.class);
		Mockito.when(crumb2.getCrumbName()).thenReturn("Crumb2");
		Mockito.when(crumb2.getCrumbType()).thenReturn("SubType");
		
		breadcrumb.pushCrumb(crumb1);
		breadcrumb.pushCrumb(crumb2);
		BreadcrumbPrinter instance = new BreadcrumbPrinter();
		instance.printBreadCrumb(stream, breadcrumb);
		
		assertEquals("(Type) Crumb1 -> (SubType) Crumb2", stream.toString());
	}
	
}

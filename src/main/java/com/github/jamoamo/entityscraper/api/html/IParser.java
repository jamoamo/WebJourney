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
package com.github.jamoamo.entityscraper.api.html;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * The interface for HTML parses. The parser is expected to return a representation of the web page it parses.
 *
 * @author James Amoore
 */
public interface IParser
{
	/**
	 * Parses the web page at the URL provided and returns a representation of its document model.
	 *
	 * @param url     The url to find the web page at.
	 * @param timeout The connection timeout.
	 *
	 * @return a representation of the document model
	 *
	 * @throws IOException if there is failure to connect to the URL.
	 */
	public AHtmlDocument parse(URL url, int timeout)
		 throws IOException;

	/**
	 * Parses the web page at the File provided and returns a representation of its document model.
	 *
	 * @param file    The file containing the web page.
	 * @param charset The character set of the file being read.
	 *
	 * @return a representation of the document model
	 *
	 * @throws IOException if there is failure to connect to the URL.
	 */
	public AHtmlDocument parse(File file, Charset charset)
		 throws IOException;

	/**
	 * Parses the web page in the provided HTML string and returns a representation of its document model.
	 *
	 * @param html The HTML string to parse.
	 *
	 * @return a representation of the document model
	 *
	 * @throws IOException if there is a failure to connect to the URL.
	 */
	public AHtmlDocument parse(String html)
		 throws IOException;

}

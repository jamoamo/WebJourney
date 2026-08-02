/*
 * The MIT License
 *
 * Copyright 2026 James Amoore.
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
package io.github.jamoamo.webjourney.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A field whose value is a text node (or a node-set of text nodes) reached by an XPath expression.
 * <p>
 * Unlike {@link ExtractValue}, which resolves its XPath to an element, this annotation's XPath is expected to
 * resolve directly to one or more text nodes - for example a bare text node sitting between two sibling elements,
 * such as {@code following-sibling::text()[1]}. This cannot be represented as a web element, so it cannot be
 * reached with {@link ExtractValue}.
 * <p>
 * Bind to a {@code String} to capture the first matching text node, or to a {@code List<String>} to capture every
 * matching text node in document order.
 * @author James Amoore
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExtractTextValue
{
	/**
	 * XPath to the text node, or node-set of text nodes, to be extracted.
	 * @return the XPath to the text node(s) to be extracted.
	 */
	String path();

	/**
	 * Is the value extraction optional? If true the extraction will result in null (or, for a
	 * {@code List<String>} field, an empty list) if no matching text node exists.
	 * @return true if the value is optional.
	 */
	boolean optional() default false;

	/**
	 * Should each extracted text value be trimmed (via {@code String.trim()}) before binding?
	 * <p>
	 * Text nodes sitting between elements in real HTML typically carry leading/trailing whitespace and
	 * newlines from the page's own formatting (e.g. {@code "\n    End of over 1: ...\n"}). Defaults to
	 * {@code true} so the common case - a single line of meaningful text - is returned clean.
	 * @return true if extracted values should be trimmed. Defaults to {@code true}.
	 */
	boolean trim() default true;
}

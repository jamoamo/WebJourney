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
package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.annotation.form.Form;
import io.github.jamoamo.webjourney.annotation.form.TextField;

/**
 *
 * @author James Amoore
 */
@Form(submit = "//input[@id='action-login']")
public class LoginForm
{
	 @TextField(xPath = "//input[@name='email']")
	 private String username;
	 @TextField(xPath = "//input[@name='password']")
	 private String password;

	 public LoginForm(String username, String password)
	 {
		  this.username = username;
		  this.password = password;
	 }

	 public String getUsername()
	 {
		  return username;
	 }

	 public void setUsername(String username)
	 {
		  this.username = username;
	 }

	 public String getPassword()
	 {
		  return password;
	 }

	 public void setPassword(String password)
	 {
		  this.password = password;
	 }

}

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
package io.github.jamoamo.webjourney.integration.testforms;

import io.github.jamoamo.webjourney.annotation.form.TextField;
import io.github.jamoamo.webjourney.annotation.form.Form;
import io.github.jamoamo.webjourney.annotation.form.Button;
import io.github.jamoamo.webjourney.annotation.form.Element;

/**
 * Test form class for integration testing of form interactions.
 *
 * @author James Amoore
 */
@Form(submit = "//button[@data-testid='submit-button']")
public class SimpleTestForm
{
    @TextField(xPath = "//input[@data-testid='first-name-input']")
    private String firstName;
    
    @TextField(xPath = "//input[@data-testid='last-name-input']")
    private String lastName;
    
    @TextField(xPath = "//input[@data-testid='email-input']")
    private String email;
    
    @TextField(xPath = "//input[@data-testid='age-input']")
    private String age;
    
    @Element(xPath = "//select[@data-testid='country-select']")
    private String country;
    
    @Element(xPath = "//textarea[@data-testid='comments-textarea']")
    private String comments;
    
    @Button(xPath = "//button[@data-testid='submit-button']")
    private String submitButton;
    
    @Button(xPath = "//button[@data-testid='reset-button']")
    private String resetButton;
    
    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public String getSubmitButton() { return submitButton; }
    public void setSubmitButton(String submitButton) { this.submitButton = submitButton; }
    
    public String getResetButton() { return resetButton; }
    public void setResetButton(String resetButton) { this.resetButton = resetButton; }
} 
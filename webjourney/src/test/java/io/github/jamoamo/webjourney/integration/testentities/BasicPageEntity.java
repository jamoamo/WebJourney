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
package io.github.jamoamo.webjourney.integration.testentities;

import io.github.jamoamo.webjourney.annotation.ExtractValue;
import io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle;

import java.util.List;

/**
 * Test entity for extracting basic page content during integration tests.
 *
 * @author James Amoore
 */
public class BasicPageEntity
{
    @ExtractFromWindowTitle
    private String title;
    
    @ExtractValue(path = "//div[@data-testid='welcome-message']", optional = true)
    private String welcomeMessage;
    
    @ExtractValue(path = "//p[@data-testid='page-content']", optional = true)
    private String pageContent;
    
    @ExtractValue(path = "//li[@data-testid]", optional = true)
    private List<String> testItems;
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getWelcomeMessage()
    {
        return welcomeMessage;
    }
    
    public void setWelcomeMessage(String welcomeMessage)
    {
        this.welcomeMessage = welcomeMessage;
    }
    
    public String getPageContent()
    {
        return pageContent;
    }
    
    public void setPageContent(String pageContent)
    {
        this.pageContent = pageContent;
    }
    
    public List<String> getTestItems()
    {
        return testItems;
    }
    
    public void setTestItems(List<String> testItems)
    {
        this.testItems = testItems;
    }
} 
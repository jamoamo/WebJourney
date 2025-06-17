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
package io.github.jamoamo.webjourney.integration;

import io.github.jamoamo.webjourney.JourneyBuilder;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.integration.testentities.FormSubmissionResult;
import io.github.jamoamo.webjourney.integration.testforms.SimpleTestForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for form interaction functionality using TestContainers.
 *
 * @author James Amoore
 */
public class FormInteractionIT extends WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FormInteractionIT.class);
    
    @Test
    @DisplayName("Should successfully complete and submit a simple form")
    public void testSimpleFormSubmission() throws Exception
    {
        LOGGER.info("Testing simple form submission...");
        
        AtomicReference<FormSubmissionResult> submissionResult = new AtomicReference<>();
        
        SimpleTestForm testForm = new SimpleTestForm();
        testForm.setFirstName("John");
        testForm.setLastName("Doe");
        testForm.setEmail("john.doe@example.com");
        testForm.setAge("30");
        testForm.setCountry("us");
        testForm.setComments("This is a test form submission from WebJourney integration tests.");
        
        IJourney journey = JourneyBuilder.path()
            .navigateTo(getTestPageUrl("forms/simple-form.html"))
            .completeFormAndSubmit(testForm)
            .consumePage(FormSubmissionResult.class, submissionResult::set)
            .build();
        
        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);
        
        // Verify form submission was successful
        FormSubmissionResult result = submissionResult.get();
        assertNotNull(result, "Form submission result should not be null");
        assertTrue(result.isSubmissionSuccessful(), "Form submission should be successful");
        assertTrue(result.getResultMessage().contains("John Doe"), 
            "Result message should contain the submitted name");
        
        LOGGER.info("Simple form submission test completed successfully");
    }
} 
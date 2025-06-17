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
import io.github.jamoamo.webjourney.integration.testentities.BasicPageEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for basic WebJourney navigation functionality using TestContainers.
 *
 * @author James Amoore
 */
public class BasicNavigationIT extends WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicNavigationIT.class);
    
    @Test
    @DisplayName("Should successfully navigate to test page and extract basic content")
    public void testBasicPageNavigation() throws Exception
    {
        LOGGER.info("Testing basic page navigation...");
        
        AtomicReference<BasicPageEntity> extractedData = new AtomicReference<>();
        
        IJourney journey = JourneyBuilder.path()
            .navigateTo(getTestPageUrl("index.html"))
            .consumePage(BasicPageEntity.class, extractedData::set)
            .build();
        
        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);
        
        // Verify the extracted data
        BasicPageEntity entity = extractedData.get();
        assertNotNull(entity, "Entity should not be null");
        assertEquals("WebJourney Integration Test Suite", entity.getTitle());
        assertEquals("Welcome to WebJourney Testing!", entity.getWelcomeMessage());
        assertNotNull(entity.getTestItems());
        assertEquals(3, entity.getTestItems().size());
        assertEquals("Test Item 1", entity.getTestItems().get(0));
        assertEquals("Test Item 2", entity.getTestItems().get(1));
        assertEquals("Test Item 3", entity.getTestItems().get(2));
        
        LOGGER.info("Basic navigation test completed successfully");
    }
    
    @Test
    @DisplayName("Should handle multi-page navigation sequence")
    public void testMultiPageNavigation() throws Exception
    {
        LOGGER.info("Testing multi-page navigation...");
        
        AtomicReference<String> page1Content = new AtomicReference<>();
        AtomicReference<String> page2Content = new AtomicReference<>();
        AtomicReference<String> page3Content = new AtomicReference<>();
        
        IJourney journey = JourneyBuilder.path()
            .navigateTo(getTestPageUrl("navigation/multi-page.html"))
            .consumePage(BasicPageEntity.class, entity -> {
                page1Content.set(entity.getPageContent());
                LOGGER.debug("Page 1 content: {}", entity.getPageContent());
            })
            .navigateTo(getTestPageUrl("navigation/page2.html"))
            .consumePage(BasicPageEntity.class, entity -> {
                page2Content.set(entity.getPageContent());
                LOGGER.debug("Page 2 content: {}", entity.getPageContent());
            })
            .navigateTo(getTestPageUrl("navigation/page3.html"))
            .consumePage(BasicPageEntity.class, entity -> {
                page3Content.set(entity.getPageContent());
                LOGGER.debug("Page 3 content: {}", entity.getPageContent());
            })
            .build();
        
        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);
        
        // Verify navigation through all pages
        assertNotNull(page1Content.get(), "Page 1 content should be extracted");
        assertNotNull(page2Content.get(), "Page 2 content should be extracted");
        assertNotNull(page3Content.get(), "Page 3 content should be extracted");
        
        assertTrue(page1Content.get().contains("first page"), 
            "Page 1 should contain expected content");
        assertTrue(page2Content.get().contains("second page"), 
            "Page 2 should contain expected content");
        assertTrue(page3Content.get().contains("WebJourney's multi-page navigation"), 
            "Page 3 should contain expected content");
        
        LOGGER.info("Multi-page navigation test completed successfully");
    }
    
    @Test
    @DisplayName("Should handle browser back and forward navigation")
    public void testBrowserNavigationControls() throws Exception
    {
        LOGGER.info("Testing browser navigation controls...");
        
        AtomicReference<String> initialPageContent = new AtomicReference<>();
        AtomicReference<String> secondPageContent = new AtomicReference<>();
        AtomicReference<String> backNavigationContent = new AtomicReference<>();
        
        IJourney journey = JourneyBuilder.path()
            .navigateTo(getTestPageUrl("navigation/multi-page.html"))
            .consumePage(BasicPageEntity.class, entity -> 
                initialPageContent.set(entity.getPageContent()))
            .navigateTo(getTestPageUrl("navigation/page2.html"))
            .consumePage(BasicPageEntity.class, entity -> 
                secondPageContent.set(entity.getPageContent()))
            .navigateBack()
            .consumePage(BasicPageEntity.class, entity -> 
                backNavigationContent.set(entity.getPageContent()))
            .build();
        
        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);
        
        // Verify navigation controls work correctly
        assertNotNull(initialPageContent.get());
        assertNotNull(secondPageContent.get());
        assertNotNull(backNavigationContent.get());
        
        // After going back, we should be on the first page again
        assertEquals(initialPageContent.get(), backNavigationContent.get(),
            "Back navigation should return to the first page");
        
        LOGGER.info("Browser navigation controls test completed successfully");
    }
} 
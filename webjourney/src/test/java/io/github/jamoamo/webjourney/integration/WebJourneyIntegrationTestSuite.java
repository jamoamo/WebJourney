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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive integration test suite for WebJourney using TestContainers.
 * This test class verifies that the test infrastructure is working correctly.
 *
 * @author James Amoore
 */
@Testcontainers
public class WebJourneyIntegrationTestSuite extends WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebJourneyIntegrationTestSuite.class);
    
    @BeforeAll
    static void setupIntegrationTests()
    {
        LOGGER.info("Initializing WebJourney Integration Test Suite...");
        LOGGER.info("Test server will be available at: {}", "http://localhost:" + 
            (nginxContainer != null ? nginxContainer.getMappedPort(NGINX_PORT) : "unknown"));
    }
    
    @Test
    @DisplayName("Should verify test infrastructure is working correctly")
    public void testInfrastructureHealth()
    {
        LOGGER.info("Verifying test infrastructure health...");
        
        // Verify the nginx container is running and accessible
        assertTrue(nginxContainer.isRunning(), "Nginx container should be running");
        
        // Verify we can access the test pages via HTTP
        given()
            .when()
                .get(getTestPageUrl("index.html"))
            .then()
                .statusCode(200)
                .body(containsString("WebJourney Integration Test Suite"));
        
        given()
            .when()
                .get(getTestPageUrl("forms/simple-form.html"))
            .then()
                .statusCode(200)
                .body(containsString("Simple Form Test"));
        
        given()
            .when()
                .get(getTestPageUrl("navigation/multi-page.html"))
            .then()
                .statusCode(200)
                .body(containsString("Multi-Page Navigation Test"));
        
        LOGGER.info("Test infrastructure health check completed successfully");
    }
    
    @Test
    @DisplayName("Should verify all test pages are accessible")
    public void testAllTestPagesAccessible()
    {
        LOGGER.info("Verifying all test pages are accessible...");
        
        String[] testPages = {
            "index.html",
            "forms/simple-form.html",
            "navigation/multi-page.html",
            "navigation/page2.html",
            "navigation/page3.html"
        };
        
        for (String page : testPages)
        {
            given()
                .when()
                    .get(getTestPageUrl(page))
                .then()
                    .statusCode(200);
            
            LOGGER.debug("Verified accessibility of page: {}", page);
        }
        
        LOGGER.info("All test pages are accessible");
    }
} 
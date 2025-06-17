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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for WebJourney integration tests using TestContainers.
 * Provides common infrastructure for spinning up containerized web applications.
 *
 * @author James Amoore
 */
@Testcontainers
public abstract class WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebJourneyTestContainerBase.class);
    
    protected static Network network;
    protected static GenericContainer<?> nginxContainer;
    
    protected static final int NGINX_PORT = 80;
    
    @BeforeAll
    static void setupTestContainers()
    {
        LOGGER.info("Setting up TestContainers infrastructure...");
        
        // Create a shared network for containers
        network = Network.newNetwork();
        
        // Setup Nginx container with test web applications
        nginxContainer = new GenericContainer<>(DockerImageName.parse("nginx:alpine"))
            .withNetwork(network)
            .withNetworkAliases("webserver")
            .withExposedPorts(NGINX_PORT)
            .withClasspathResourceMapping("integration-test-sites", "/usr/share/nginx/html", 
                org.testcontainers.containers.BindMode.READ_ONLY)
            .waitingFor(Wait.forHttp("/").forStatusCode(200));
        
        nginxContainer.start();
        
        LOGGER.info("TestContainers infrastructure ready. Nginx available at: http://localhost:{}", 
            nginxContainer.getMappedPort(NGINX_PORT));
    }
    
    @AfterAll
    static void tearDownTestContainers()
    {
        LOGGER.info("Tearing down TestContainers infrastructure...");
        
        if (nginxContainer != null && nginxContainer.isRunning())
        {
            nginxContainer.stop();
        }
        
        if (network != null)
        {
            network.close();
        }
    }
    
    /**
     * Get the base URL for the test web server.
     * @return Base URL for accessing test applications
     */
    protected String getBaseUrl()
    {
        return "http://localhost:" + nginxContainer.getMappedPort(NGINX_PORT);
    }
    
    /**
     * Get URL for a specific test page.
     * @param pagePath Path to the test page
     * @return Full URL to the test page
     */
    protected String getTestPageUrl(String pagePath)
    {
        return getBaseUrl() + "/" + pagePath;
    }
    
    /**
     * Get the dynamically mapped port of the Nginx container.
     * @return The mapped port.
     */
    protected int getNginxPort()
    {
        return nginxContainer.getMappedPort(NGINX_PORT);
    }
} 
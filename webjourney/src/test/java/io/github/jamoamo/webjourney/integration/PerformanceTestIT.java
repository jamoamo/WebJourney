package io.github.jamoamo.webjourney.integration;

import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.JourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.Properties;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * Integration tests for basic WebJourney navigation functionality using TestContainers.
 *
 * @author James Amoore
 */
public class PerformanceTestIT extends WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTestIT.class);
    
    @Test
    @DisplayName("Should successfully run a basic JMeter performance test")
    public void testBasicJMeterPerformanceTest() throws Exception
    {
        LOGGER.info("Running basic JMeter performance test...");

        // Get the dynamically assigned port from the Nginx container
        int nginxPort = getNginxPort();
        LOGGER.info("Nginx container port: {}", nginxPort);

        // Set JMeter properties to pass the dynamic port
        Properties properties = new Properties();
        properties.setProperty("test_server_port", String.valueOf(nginxPort));

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("pom.xml")); // Path to the project pom.xml
        request.setGoals(java.util.Arrays.asList("jmeter:jmeter"));
        request.setProperties(properties);
        
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("c:\\Program Files\\Apache\\apache-maven-3.9.8"));

        try
        {
            invoker.execute(request);
            LOGGER.info("JMeter performance test completed successfully.");
        }
        catch (MavenInvocationException e)
        {
            LOGGER.error("JMeter performance test failed: ", e);
            throw e;
        }
    }
} 
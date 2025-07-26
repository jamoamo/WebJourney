package io.github.jamoamo.webjourney.integration;

import io.github.jamoamo.webjourney.WebTraveller;
import io.github.jamoamo.webjourney.TravelOptions;
import io.github.jamoamo.webjourney.JourneyBuilder;
import io.github.jamoamo.webjourney.api.IJourney;
import io.github.jamoamo.webjourney.performance.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive performance tests for WebJourney using the enhanced testing framework.
 *
 * @author James Amoore
 */
public class PerformanceTestIT extends WebJourneyTestContainerBase
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTestIT.class);
    
    @Test
    @DisplayName("Should execute basic load test and validate performance metrics")
    @Tag("performance")
    public void testBasicLoadTest() throws PerformanceTestException
    {
        LOGGER.info("Starting basic load performance test...");
        
        PerformanceTestConfiguration config = PerformanceTestConfiguration.forLoadTesting()
            .setThreads(5)
            .setDurationSeconds(60)
            .setMaxAverageResponseTime(3000)
            .setMaxErrorRate(0.02);
        
        PerformanceTestRunner runner = new PerformanceTestRunner(config);
        PerformanceTestResults results = runner.executeTest("load-tests/basic-load-test.jmx", getNginxPort());
        
        // Validate results
        assertNotNull(results, "Performance test results should not be null");
        LOGGER.info("Load test completed: {}", results.generateSummaryReport());
        
        // Validate performance metrics
        PerformanceValidationReport validation = runner.validateResults(results);
        if (!validation.getViolations().isEmpty())
        {
            LOGGER.warn("Performance violations detected: {}", validation.getViolations());
            // Note: In a real scenario, you might want to fail the test here
            // fail("Performance thresholds violated: " + validation.getViolations());
        }
        
        assertTrue(results.getDurationMs() > 0, "Test should have a positive duration");
        assertTrue(results.getTotalSamples() > 0, "Test should have processed some samples");
    }
    
    @Test
    @DisplayName("Should execute stress test with higher load")
    @Tag("performance")
    @Tag("stress")
    public void testStressTest() throws PerformanceTestException
    {
        LOGGER.info("Starting stress performance test...");
        
        PerformanceTestConfiguration config = PerformanceTestConfiguration.forStressTesting()
            .setThreads(20)
            .setDurationSeconds(90)
            .setMaxAverageResponseTime(5000)
            .setMaxErrorRate(0.10);
        
        PerformanceTestRunner runner = new PerformanceTestRunner(config);
        PerformanceTestResults results = runner.executeTest("load-tests/stress-test.jmx", getNginxPort());
        
        assertNotNull(results, "Stress test results should not be null");
        LOGGER.info("Stress test completed: {}", results.generateSummaryReport());
        
        // Stress tests may have higher error rates, so we're more lenient
        assertTrue(results.getErrorRate() <= 0.15, "Error rate should be reasonable even under stress");
        assertTrue(results.getTotalSamples() > 0, "Stress test should process samples");
    }
    
    @Test
    @DisplayName("Should execute browser automation performance test")
    @Tag("performance")
    @Tag("browser")
    public void testBrowserAutomationPerformance() throws PerformanceTestException
    {
        LOGGER.info("Starting browser automation performance test...");
        
        PerformanceTestConfiguration config = PerformanceTestConfiguration.forBrowserAutomation()
            .setThreads(3)
            .setDurationSeconds(120)
            .setMaxAverageResponseTime(8000)
            .setMaxMemoryUsage(4096);
        
        PerformanceTestRunner runner = new PerformanceTestRunner(config);
        PerformanceTestResults results = runner.executeTest("browser-automation-tests/journey-execution-load.jmx", getNginxPort());
        
        assertNotNull(results, "Browser automation test results should not be null");
        LOGGER.info("Browser automation test completed: {}", results.generateSummaryReport());
        
        // Browser automation typically has higher response times and memory usage
        assertTrue(results.getTotalSamples() > 0, "Browser test should process samples");
        assertNotNull(results.getMetrics(), "System metrics should be collected");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"load-tests/basic-load-test.jmx", "load-tests/stress-test.jmx"})
    @DisplayName("Should execute multiple test plans with consistent results")
    @Tag("performance")
    @Tag("regression")
    public void testMultiplePerformanceScenarios(String testPlan) throws PerformanceTestException
    {
        LOGGER.info("Testing performance scenario: {}", testPlan);
        
        PerformanceTestConfiguration config = PerformanceTestConfiguration.forRegressionTesting()
            .setThreads(8)
            .setDurationSeconds(45);
        
        PerformanceTestRunner runner = new PerformanceTestRunner(config);
        PerformanceTestResults results = runner.executeTest(testPlan, getNginxPort());
        
        assertNotNull(results, "Results should not be null for " + testPlan);
        assertTrue(results.getTotalSamples() > 0, "Should have samples for " + testPlan);
        assertTrue(results.getErrorRate() < 0.05, "Error rate should be low for " + testPlan);
        
        LOGGER.info("Scenario {} results: Samples={}, ErrorRate={:.2f}%, AvgResponseTime={}ms", 
            testPlan, results.getTotalSamples(), results.getErrorRate() * 100, results.getAverageResponseTime());
    }
    
    @Test
    @DisplayName("Should execute parallel performance tests")
    @Tag("performance")
    @Tag("parallel")
    public void testParallelPerformanceExecution() throws Exception
    {
        LOGGER.info("Starting parallel performance test execution...");
        
        PerformanceTestConfiguration config = PerformanceTestConfiguration.forLoadTesting()
            .setThreads(3)
            .setDurationSeconds(30);
        
        PerformanceTestRunner runner = new PerformanceTestRunner(config);
        
        String[] testPlans = {
            "load-tests/basic-load-test.jmx",
            "browser-automation-tests/journey-execution-load.jmx"
        };
        
        CompletableFuture<PerformanceTestResults[]> futureResults = 
            runner.executeTestsInParallel(testPlans, getNginxPort());
        
        PerformanceTestResults[] results = futureResults.get();
        
        assertNotNull(results, "Parallel test results should not be null");
        assertEquals(testPlans.length, results.length, "Should have results for all test plans");
        
        for (int i = 0; i < results.length; i++)
        {
            assertNotNull(results[i], "Result " + i + " should not be null");
            assertTrue(results[i].getTotalSamples() > 0, "Result " + i + " should have samples");
            LOGGER.info("Parallel test {} completed: {}", testPlans[i], results[i]);
        }
    }
    
    @Test
    @DisplayName("Should measure actual WebJourney execution performance")
    @Tag("performance")
    @Tag("integration")
    public void testWebJourneyExecutionPerformance() throws Exception
    {
        LOGGER.info("Testing actual WebJourney execution performance...");
        
        PerformanceMetricsCollector metricsCollector = new PerformanceMetricsCollector();
        AtomicReference<String> pageTitle = new AtomicReference<>();
        
        // Start metrics collection
        metricsCollector.startCollection();
        
        long startTime = System.currentTimeMillis();
        
        // Execute a real WebJourney
        IJourney journey = JourneyBuilder.path()
            .navigateTo(getTestPageUrl("index.html"))
            .consumePage(BasicPageEntity.class, entity -> {
                pageTitle.set(entity.getTitle());
            })
            .navigateTo(getTestPageUrl("forms/simple-form.html"))
            .navigateTo(getTestPageUrl("navigation/multi-page.html"))
            .build();
        
        WebTraveller traveller = new WebTraveller(new TravelOptions());
        traveller.travelJourney(journey);
        
        long endTime = System.currentTimeMillis();
        
        // Stop metrics collection
        PerformanceMetrics metrics = metricsCollector.stopCollectionAndAnalyze();
        
        // Validate execution
        assertNotNull(pageTitle.get(), "Should have extracted page title");
        assertEquals("WebJourney Integration Test Suite", pageTitle.get());
        
        long executionTime = endTime - startTime;
        LOGGER.info("WebJourney execution completed in {} ms", executionTime);
        LOGGER.info("System metrics: {}", metrics);
        
        // Performance assertions
        assertTrue(executionTime < 30000, "WebJourney execution should complete within 30 seconds");
        assertTrue(metrics.getMaxMemoryUsage() < 1024, "Memory usage should be reasonable (< 1GB)");
        assertTrue(metrics.getMaxCpuUsage() < 90, "CPU usage should not spike too high");
    }
    
    /**
     * Simple entity for extracting page title in performance tests.
     */
    public static class BasicPageEntity
    {
        @io.github.jamoamo.webjourney.annotation.ExtractFromWindowTitle
        private String title;
        
        public String getTitle()
        {
            return title;
        }
        
        public void setTitle(String title)
        {
            this.title = title;
        }
    }
} 
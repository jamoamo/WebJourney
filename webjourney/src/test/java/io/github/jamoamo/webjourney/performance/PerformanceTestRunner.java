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
package io.github.jamoamo.webjourney.performance;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Performance test runner that orchestrates JMeter execution with TestContainers
 * and provides comprehensive metrics collection and analysis.
 *
 * @author James Amoore
 */
public class PerformanceTestRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceTestRunner.class);
    
    private final PerformanceTestConfiguration config;
    private final PerformanceMetricsCollector metricsCollector;
    
    public PerformanceTestRunner(PerformanceTestConfiguration config)
    {
        this.config = config;
        this.metricsCollector = new PerformanceMetricsCollector();
    }
    
    /**
     * Executes a performance test with the specified test plan.
     *
     * @param testPlanName Name of the JMeter test plan to execute
     * @param testServerPort Port of the test server
     * @return Performance test results
     * @throws PerformanceTestException if test execution fails
     */
    public PerformanceTestResults executeTest(String testPlanName, int testServerPort) 
        throws PerformanceTestException
    {
        LOGGER.info("Starting performance test: {}", testPlanName);
        
        try
        {
            // Prepare test environment
            Properties testProperties = prepareTestProperties(testServerPort);
            
            // Start metrics collection
            metricsCollector.startCollection();
            
            // Execute JMeter test
            long startTime = System.currentTimeMillis();
            executeJMeterTest(testPlanName, testProperties);
            long endTime = System.currentTimeMillis();
            
            // Stop metrics collection and analyze results
            PerformanceMetrics metrics = metricsCollector.stopCollectionAndAnalyze();
            
            // Parse JMeter results
            PerformanceTestResults results = parseJMeterResults(testPlanName, startTime, endTime, metrics);
            
            LOGGER.info("Performance test completed: {} in {} ms", testPlanName, endTime - startTime);
            return results;
        }
        catch (Exception e)
        {
            LOGGER.error("Performance test failed: {}", testPlanName, e);
            throw new PerformanceTestException("Failed to execute performance test: " + testPlanName, e);
        }
    }
    
    /**
     * Executes multiple performance tests in parallel.
     *
     * @param testPlans Array of test plan names to execute
     * @param testServerPort Port of the test server
     * @return Array of performance test results
     */
    public CompletableFuture<PerformanceTestResults[]> executeTestsInParallel(
        String[] testPlans, int testServerPort)
    {
        LOGGER.info("Starting parallel performance tests: {}", Arrays.toString(testPlans));
        
        CompletableFuture<PerformanceTestResults>[] futures = new CompletableFuture[testPlans.length];
        
        for (int i = 0; i < testPlans.length; i++)
        {
            final String testPlan = testPlans[i];
            futures[i] = CompletableFuture.supplyAsync(() -> {
                try
                {
                    return executeTest(testPlan, testServerPort);
                }
                catch (PerformanceTestException e)
                {
                    LOGGER.error("Parallel test failed: {}", testPlan, e);
                    throw new RuntimeException(e);
                }
            });
        }
        
        return CompletableFuture.allOf(futures)
            .thenApply(v -> Arrays.stream(futures)
                .map(CompletableFuture::join)
                .toArray(PerformanceTestResults[]::new));
    }
    
    /**
     * Validates performance test results against defined thresholds.
     *
     * @param results Performance test results to validate
     * @return Validation report
     */
    public PerformanceValidationReport validateResults(PerformanceTestResults results)
    {
        LOGGER.info("Validating performance test results for: {}", results.getTestName());
        
        PerformanceValidationReport report = new PerformanceValidationReport(results.getTestName());
        
        // Validate response time thresholds
        if (results.getAverageResponseTime() > config.getMaxAverageResponseTime())
        {
            report.addViolation("Average response time exceeded threshold: " + 
                results.getAverageResponseTime() + "ms > " + config.getMaxAverageResponseTime() + "ms");
        }
        
        if (results.getMaxResponseTime() > config.getMaxResponseTime())
        {
            report.addViolation("Maximum response time exceeded threshold: " + 
                results.getMaxResponseTime() + "ms > " + config.getMaxResponseTime() + "ms");
        }
        
        // Validate error rate thresholds
        if (results.getErrorRate() > config.getMaxErrorRate())
        {
            report.addViolation("Error rate exceeded threshold: " + 
                String.format("%.2f%% > %.2f%%", results.getErrorRate() * 100, config.getMaxErrorRate() * 100));
        }
        
        // Validate throughput thresholds
        if (results.getThroughput() < config.getMinThroughput())
        {
            report.addViolation("Throughput below threshold: " + 
                String.format("%.2f req/sec < %.2f req/sec", results.getThroughput(), config.getMinThroughput()));
        }
        
        // Validate resource usage
        PerformanceMetrics metrics = results.getMetrics();
        if (metrics.getMaxMemoryUsage() > config.getMaxMemoryUsage())
        {
            report.addViolation("Memory usage exceeded threshold: " + 
                metrics.getMaxMemoryUsage() + "MB > " + config.getMaxMemoryUsage() + "MB");
        }
        
        if (metrics.getMaxCpuUsage() > config.getMaxCpuUsage())
        {
            report.addViolation("CPU usage exceeded threshold: " + 
                String.format("%.1f%% > %.1f%%", metrics.getMaxCpuUsage(), config.getMaxCpuUsage()));
        }
        
        LOGGER.info("Performance validation completed. Violations: {}", report.getViolations().size());
        return report;
    }
    
    private Properties prepareTestProperties(int testServerPort)
    {
        Properties properties = new Properties();
        properties.setProperty("webjourney.test.baseurl", "http://localhost:" + testServerPort);
        properties.setProperty("webjourney.threads", String.valueOf(config.getThreads()));
        properties.setProperty("webjourney.rampup", String.valueOf(config.getRampUpSeconds()));
        properties.setProperty("webjourney.duration", String.valueOf(config.getDurationSeconds()));
        properties.setProperty("webjourney.stress.threads", String.valueOf(config.getStressThreads()));
        properties.setProperty("webjourney.stress.rampup", String.valueOf(config.getStressRampUpSeconds()));
        properties.setProperty("webjourney.stress.duration", String.valueOf(config.getStressDurationSeconds()));
        properties.setProperty("webjourney.browser.threads", String.valueOf(config.getBrowserThreads()));
        properties.setProperty("webjourney.browser.rampup", String.valueOf(config.getBrowserRampUpSeconds()));
        properties.setProperty("webjourney.browser.duration", String.valueOf(config.getBrowserDurationSeconds()));
        
        return properties;
    }
    
    private void executeJMeterTest(String testPlanName, Properties testProperties) 
        throws MavenInvocationException
    {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("webjourney/pom.xml"));
        request.setGoals(Arrays.asList("jmeter:jmeter"));
        request.setProperties(testProperties);
        
        // Add test-specific properties
        Properties jmeterProperties = new Properties();
        jmeterProperties.putAll(testProperties);
        jmeterProperties.setProperty("jmeter.testplan", testPlanName);
        request.setProperties(jmeterProperties);
        
        Invoker invoker = new DefaultInvoker();
        
        // Try to find Maven installation automatically
        String mavenHome = findMavenHome();
        if (mavenHome != null)
        {
            invoker.setMavenHome(new File(mavenHome));
        }
        
        invoker.execute(request);
    }
    
    private String findMavenHome()
    {
        // Try common Maven installation paths
        String[] commonPaths = {
            "C:\\Program Files\\Apache\\apache-maven-3.9.8",
            "C:\\Program Files\\Maven\\apache-maven-3.9.8",
            "C:\\maven",
            "/usr/local/maven",
            "/opt/maven"
        };
        
        for (String path : commonPaths)
        {
            if (new File(path).exists())
            {
                return path;
            }
        }
        
        // Try environment variables
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null && new File(mavenHome).exists())
        {
            return mavenHome;
        }
        
        mavenHome = System.getenv("M2_HOME");
        if (mavenHome != null && new File(mavenHome).exists())
        {
            return mavenHome;
        }
        
        LOGGER.warn("Could not find Maven installation. JMeter execution may fail.");
        return null;
    }
    
    private PerformanceTestResults parseJMeterResults(String testPlanName, long startTime, long endTime, 
        PerformanceMetrics metrics) throws PerformanceTestException
    {
        try
        {
            // Parse JMeter result files (JTL format)
            Path resultsDir = Paths.get("webjourney/target/jmeter-results");
            JMeterResultsParser parser = new JMeterResultsParser(resultsDir);
            
            return parser.parseResults(testPlanName, startTime, endTime, metrics);
        }
        catch (Exception e)
        {
            throw new PerformanceTestException("Failed to parse JMeter results for: " + testPlanName, e);
        }
    }
} 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Collects system metrics during performance test execution.
 * Monitors CPU usage, memory consumption, and other system resources.
 *
 * @author James Amoore
 */
public class PerformanceMetricsCollector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceMetricsCollector.class);
    
    private final MemoryMXBean memoryBean;
    private final OperatingSystemMXBean osBean;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean collecting;
    
    private final List<Double> cpuUsageReadings;
    private final List<Long> memoryUsageReadings;
    private final List<Long> freeMemoryReadings;
    
    private long startTime;
    private long endTime;
    
    public PerformanceMetricsCollector()
    {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.collecting = new AtomicBoolean(false);
        
        this.cpuUsageReadings = new ArrayList<>();
        this.memoryUsageReadings = new ArrayList<>();
        this.freeMemoryReadings = new ArrayList<>();
    }
    
    /**
     * Starts collecting system metrics at regular intervals.
     */
    public void startCollection()
    {
        if (collecting.compareAndSet(false, true))
        {
            LOGGER.info("Starting performance metrics collection");
            startTime = System.currentTimeMillis();
            
            // Clear previous readings
            cpuUsageReadings.clear();
            memoryUsageReadings.clear();
            freeMemoryReadings.clear();
            
            // Schedule metrics collection every second
            scheduler.scheduleAtFixedRate(this::collectMetrics, 0, 1, TimeUnit.SECONDS);
        }
    }
    
    /**
     * Stops collecting metrics and returns the analyzed results.
     *
     * @return Performance metrics analysis
     */
    public PerformanceMetrics stopCollectionAndAnalyze()
    {
        if (collecting.compareAndSet(true, false))
        {
            endTime = System.currentTimeMillis();
            scheduler.shutdown();
            
            try
            {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                {
                    scheduler.shutdownNow();
                }
            }
            catch (InterruptedException e)
            {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            LOGGER.info("Stopped performance metrics collection. Duration: {} ms", endTime - startTime);
            return analyzeMetrics();
        }
        
        return new PerformanceMetrics(); // Empty metrics if not collecting
    }
    
    private void collectMetrics()
    {
        try
        {
            // Collect CPU usage
            double cpuUsage = getCpuUsage();
            if (cpuUsage >= 0)
            {
                cpuUsageReadings.add(cpuUsage);
            }
            
            // Collect memory usage
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024); // MB
            memoryUsageReadings.add(usedMemory);
            
            // Collect free memory
            long freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024); // MB
            freeMemoryReadings.add(freeMemory);
            
        }
        catch (Exception e)
        {
            LOGGER.warn("Error collecting metrics: {}", e.getMessage());
        }
    }
    
    private double getCpuUsage()
    {
        if (osBean instanceof com.sun.management.OperatingSystemMXBean)
        {
            com.sun.management.OperatingSystemMXBean sunBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            return sunBean.getProcessCpuLoad() * 100.0;
        }
        else
        {
            // Fallback for non-Sun JVMs
            return osBean.getSystemLoadAverage();
        }
    }
    
    private PerformanceMetrics analyzeMetrics()
    {
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        metrics.setCollectionDuration(endTime - startTime);
        metrics.setDataPoints(cpuUsageReadings.size());
        
        // Analyze CPU usage
        if (!cpuUsageReadings.isEmpty())
        {
            metrics.setAverageCpuUsage(cpuUsageReadings.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0));
            
            metrics.setMaxCpuUsage(cpuUsageReadings.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0));
            
            metrics.setMinCpuUsage(cpuUsageReadings.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0));
        }
        
        // Analyze memory usage
        if (!memoryUsageReadings.isEmpty())
        {
            metrics.setAverageMemoryUsage(memoryUsageReadings.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0));
            
            metrics.setMaxMemoryUsage(memoryUsageReadings.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L));
            
            metrics.setMinMemoryUsage(memoryUsageReadings.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L));
        }
        
        // Calculate memory utilization trends
        if (memoryUsageReadings.size() > 1)
        {
            long initialMemory = memoryUsageReadings.get(0);
            long finalMemory = memoryUsageReadings.get(memoryUsageReadings.size() - 1);
            metrics.setMemoryGrowth(finalMemory - initialMemory);
        }
        
        LOGGER.info("Performance metrics analysis completed: {}", metrics);
        return metrics;
    }
    
    /**
     * Checks if metrics collection is currently active.
     *
     * @return true if collecting metrics, false otherwise
     */
    public boolean isCollecting()
    {
        return collecting.get();
    }
    
    /**
     * Forces shutdown of the metrics collector.
     */
    public void shutdown()
    {
        collecting.set(false);
        scheduler.shutdownNow();
    }
} 
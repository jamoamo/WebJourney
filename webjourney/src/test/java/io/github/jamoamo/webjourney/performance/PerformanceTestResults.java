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

import java.time.LocalDateTime;

/**
 * Comprehensive performance test results including both JMeter metrics
 * and system resource usage data.
 *
 * @author James Amoore
 */
public class PerformanceTestResults
{
    private String testName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
    
    // JMeter metrics
    private long totalSamples;
    private long successfulSamples;
    private long failedSamples;
    private double errorRate;
    
    // Response time metrics
    private long averageResponseTime;
    private long minResponseTime;
    private long maxResponseTime;
    private long p90ResponseTime;
    private long p95ResponseTime;
    private long p99ResponseTime;
    
    // Throughput metrics
    private double throughput; // requests per second
    private double bytesPerSecond;
    
    // System metrics
    private PerformanceMetrics metrics;
    
    public PerformanceTestResults(String testName)
    {
        this.testName = testName;
        this.startTime = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getTestName()
    {
        return testName;
    }
    
    public void setTestName(String testName)
    {
        this.testName = testName;
    }
    
    public LocalDateTime getStartTime()
    {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime)
    {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime()
    {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime)
    {
        this.endTime = endTime;
    }
    
    public long getDurationMs()
    {
        return durationMs;
    }
    
    public void setDurationMs(long durationMs)
    {
        this.durationMs = durationMs;
    }
    
    public long getTotalSamples()
    {
        return totalSamples;
    }
    
    public void setTotalSamples(long totalSamples)
    {
        this.totalSamples = totalSamples;
    }
    
    public long getSuccessfulSamples()
    {
        return successfulSamples;
    }
    
    public void setSuccessfulSamples(long successfulSamples)
    {
        this.successfulSamples = successfulSamples;
    }
    
    public long getFailedSamples()
    {
        return failedSamples;
    }
    
    public void setFailedSamples(long failedSamples)
    {
        this.failedSamples = failedSamples;
    }
    
    public double getErrorRate()
    {
        return errorRate;
    }
    
    public void setErrorRate(double errorRate)
    {
        this.errorRate = errorRate;
    }
    
    public long getAverageResponseTime()
    {
        return averageResponseTime;
    }
    
    public void setAverageResponseTime(long averageResponseTime)
    {
        this.averageResponseTime = averageResponseTime;
    }
    
    public long getMinResponseTime()
    {
        return minResponseTime;
    }
    
    public void setMinResponseTime(long minResponseTime)
    {
        this.minResponseTime = minResponseTime;
    }
    
    public long getMaxResponseTime()
    {
        return maxResponseTime;
    }
    
    public void setMaxResponseTime(long maxResponseTime)
    {
        this.maxResponseTime = maxResponseTime;
    }
    
    public long getP90ResponseTime()
    {
        return p90ResponseTime;
    }
    
    public void setP90ResponseTime(long p90ResponseTime)
    {
        this.p90ResponseTime = p90ResponseTime;
    }
    
    public long getP95ResponseTime()
    {
        return p95ResponseTime;
    }
    
    public void setP95ResponseTime(long p95ResponseTime)
    {
        this.p95ResponseTime = p95ResponseTime;
    }
    
    public long getP99ResponseTime()
    {
        return p99ResponseTime;
    }
    
    public void setP99ResponseTime(long p99ResponseTime)
    {
        this.p99ResponseTime = p99ResponseTime;
    }
    
    public double getThroughput()
    {
        return throughput;
    }
    
    public void setThroughput(double throughput)
    {
        this.throughput = throughput;
    }
    
    public double getBytesPerSecond()
    {
        return bytesPerSecond;
    }
    
    public void setBytesPerSecond(double bytesPerSecond)
    {
        this.bytesPerSecond = bytesPerSecond;
    }
    
    public PerformanceMetrics getMetrics()
    {
        return metrics;
    }
    
    public void setMetrics(PerformanceMetrics metrics)
    {
        this.metrics = metrics;
    }
    
    /**
     * Calculates derived metrics from the raw data.
     */
    public void calculateDerivedMetrics()
    {
        if (totalSamples > 0)
        {
            this.errorRate = (double) failedSamples / totalSamples;
        }
        
        if (durationMs > 0)
        {
            this.throughput = (double) totalSamples / (durationMs / 1000.0);
        }
    }
    
    /**
     * Generates a summary report of the performance test results.
     *
     * @return Formatted summary string
     */
    public String generateSummaryReport()
    {
        StringBuilder report = new StringBuilder();
        report.append("=== Performance Test Results Summary ===\n");
        report.append("Test Name: ").append(testName).append("\n");
        report.append("Duration: ").append(durationMs).append(" ms\n");
        report.append("Total Samples: ").append(totalSamples).append("\n");
        report.append("Success Rate: ").append(String.format("%.2f%%", (1 - errorRate) * 100)).append("\n");
        report.append("Error Rate: ").append(String.format("%.2f%%", errorRate * 100)).append("\n");
        report.append("Average Response Time: ").append(averageResponseTime).append(" ms\n");
        report.append("Max Response Time: ").append(maxResponseTime).append(" ms\n");
        report.append("P95 Response Time: ").append(p95ResponseTime).append(" ms\n");
        report.append("P99 Response Time: ").append(p99ResponseTime).append(" ms\n");
        report.append("Throughput: ").append(String.format("%.2f", throughput)).append(" req/sec\n");
        
        if (metrics != null)
        {
            report.append("\n=== System Metrics ===\n");
            report.append("Max CPU Usage: ").append(String.format("%.2f%%", metrics.getMaxCpuUsage())).append("\n");
            report.append("Avg CPU Usage: ").append(String.format("%.2f%%", metrics.getAverageCpuUsage())).append("\n");
            report.append("Max Memory Usage: ").append(metrics.getMaxMemoryUsage()).append(" MB\n");
            report.append("Avg Memory Usage: ").append(String.format("%.2f MB", metrics.getAverageMemoryUsage())).append("\n");
            report.append("Memory Growth: ").append(metrics.getMemoryGrowth()).append(" MB\n");
        }
        
        return report.toString();
    }
    
    @Override
    public String toString()
    {
        return "PerformanceTestResults{" +
            "testName='" + testName + '\'' +
            ", durationMs=" + durationMs +
            ", totalSamples=" + totalSamples +
            ", errorRate=" + String.format("%.4f", errorRate) +
            ", averageResponseTime=" + averageResponseTime +
            ", maxResponseTime=" + maxResponseTime +
            ", throughput=" + String.format("%.2f", throughput) +
            '}';
    }
} 
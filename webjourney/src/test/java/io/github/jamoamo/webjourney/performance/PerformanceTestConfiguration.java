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

/**
 * Configuration class for performance testing parameters and thresholds.
 * Provides sensible defaults for WebJourney performance testing.
 *
 * @author James Amoore
 */
public class PerformanceTestConfiguration
{
    // Load test configuration
    private int threads = 10;
    private int rampUpSeconds = 60;
    private int durationSeconds = 300;
    
    // Stress test configuration
    private int stressThreads = 50;
    private int stressRampUpSeconds = 120;
    private int stressDurationSeconds = 600;
    
    // Browser automation test configuration
    private int browserThreads = 5;
    private int browserRampUpSeconds = 30;
    private int browserDurationSeconds = 300;
    
    // Performance thresholds
    private long maxAverageResponseTime = 2000; // ms
    private long maxResponseTime = 10000; // ms
    private double maxErrorRate = 0.05; // 5%
    private double minThroughput = 1.0; // requests per second
    
    // Resource usage thresholds
    private long maxMemoryUsage = 2048; // MB
    private double maxCpuUsage = 80.0; // %
    
    public PerformanceTestConfiguration()
    {
        // Default constructor with sensible defaults
    }
    
    /**
     * Creates a configuration for basic load testing.
     */
    public static PerformanceTestConfiguration forLoadTesting()
    {
        PerformanceTestConfiguration config = new PerformanceTestConfiguration();
        config.threads = 10;
        config.rampUpSeconds = 60;
        config.durationSeconds = 300;
        config.maxAverageResponseTime = 2000;
        config.maxErrorRate = 0.01; // 1%
        return config;
    }
    
    /**
     * Creates a configuration for stress testing.
     */
    public static PerformanceTestConfiguration forStressTesting()
    {
        PerformanceTestConfiguration config = new PerformanceTestConfiguration();
        config.threads = 50;
        config.rampUpSeconds = 120;
        config.durationSeconds = 600;
        config.maxAverageResponseTime = 5000;
        config.maxErrorRate = 0.10; // 10%
        return config;
    }
    
    /**
     * Creates a configuration for browser automation testing.
     */
    public static PerformanceTestConfiguration forBrowserAutomation()
    {
        PerformanceTestConfiguration config = new PerformanceTestConfiguration();
        config.threads = 5;
        config.rampUpSeconds = 30;
        config.durationSeconds = 300;
        config.maxAverageResponseTime = 5000; // Browser automation is slower
        config.maxErrorRate = 0.02; // 2%
        config.maxMemoryUsage = 4096; // Browser automation uses more memory
        return config;
    }
    
    /**
     * Creates a configuration for regression testing.
     */
    public static PerformanceTestConfiguration forRegressionTesting()
    {
        PerformanceTestConfiguration config = new PerformanceTestConfiguration();
        config.threads = 20;
        config.rampUpSeconds = 60;
        config.durationSeconds = 180;
        config.maxAverageResponseTime = 1500;
        config.maxErrorRate = 0.005; // 0.5%
        config.minThroughput = 5.0;
        return config;
    }
    
    // Getters and setters
    public int getThreads()
    {
        return threads;
    }
    
    public PerformanceTestConfiguration setThreads(int threads)
    {
        this.threads = threads;
        return this;
    }
    
    public int getRampUpSeconds()
    {
        return rampUpSeconds;
    }
    
    public PerformanceTestConfiguration setRampUpSeconds(int rampUpSeconds)
    {
        this.rampUpSeconds = rampUpSeconds;
        return this;
    }
    
    public int getDurationSeconds()
    {
        return durationSeconds;
    }
    
    public PerformanceTestConfiguration setDurationSeconds(int durationSeconds)
    {
        this.durationSeconds = durationSeconds;
        return this;
    }
    
    public int getStressThreads()
    {
        return stressThreads;
    }
    
    public PerformanceTestConfiguration setStressThreads(int stressThreads)
    {
        this.stressThreads = stressThreads;
        return this;
    }
    
    public int getStressRampUpSeconds()
    {
        return stressRampUpSeconds;
    }
    
    public PerformanceTestConfiguration setStressRampUpSeconds(int stressRampUpSeconds)
    {
        this.stressRampUpSeconds = stressRampUpSeconds;
        return this;
    }
    
    public int getStressDurationSeconds()
    {
        return stressDurationSeconds;
    }
    
    public PerformanceTestConfiguration setStressDurationSeconds(int stressDurationSeconds)
    {
        this.stressDurationSeconds = stressDurationSeconds;
        return this;
    }
    
    public int getBrowserThreads()
    {
        return browserThreads;
    }
    
    public PerformanceTestConfiguration setBrowserThreads(int browserThreads)
    {
        this.browserThreads = browserThreads;
        return this;
    }
    
    public int getBrowserRampUpSeconds()
    {
        return browserRampUpSeconds;
    }
    
    public PerformanceTestConfiguration setBrowserRampUpSeconds(int browserRampUpSeconds)
    {
        this.browserRampUpSeconds = browserRampUpSeconds;
        return this;
    }
    
    public int getBrowserDurationSeconds()
    {
        return browserDurationSeconds;
    }
    
    public PerformanceTestConfiguration setBrowserDurationSeconds(int browserDurationSeconds)
    {
        this.browserDurationSeconds = browserDurationSeconds;
        return this;
    }
    
    public long getMaxAverageResponseTime()
    {
        return maxAverageResponseTime;
    }
    
    public PerformanceTestConfiguration setMaxAverageResponseTime(long maxAverageResponseTime)
    {
        this.maxAverageResponseTime = maxAverageResponseTime;
        return this;
    }
    
    public long getMaxResponseTime()
    {
        return maxResponseTime;
    }
    
    public PerformanceTestConfiguration setMaxResponseTime(long maxResponseTime)
    {
        this.maxResponseTime = maxResponseTime;
        return this;
    }
    
    public double getMaxErrorRate()
    {
        return maxErrorRate;
    }
    
    public PerformanceTestConfiguration setMaxErrorRate(double maxErrorRate)
    {
        this.maxErrorRate = maxErrorRate;
        return this;
    }
    
    public double getMinThroughput()
    {
        return minThroughput;
    }
    
    public PerformanceTestConfiguration setMinThroughput(double minThroughput)
    {
        this.minThroughput = minThroughput;
        return this;
    }
    
    public long getMaxMemoryUsage()
    {
        return maxMemoryUsage;
    }
    
    public PerformanceTestConfiguration setMaxMemoryUsage(long maxMemoryUsage)
    {
        this.maxMemoryUsage = maxMemoryUsage;
        return this;
    }
    
    public double getMaxCpuUsage()
    {
        return maxCpuUsage;
    }
    
    public PerformanceTestConfiguration setMaxCpuUsage(double maxCpuUsage)
    {
        this.maxCpuUsage = maxCpuUsage;
        return this;
    }
    
    @Override
    public String toString()
    {
        return "PerformanceTestConfiguration{" +
            "threads=" + threads +
            ", rampUpSeconds=" + rampUpSeconds +
            ", durationSeconds=" + durationSeconds +
            ", stressThreads=" + stressThreads +
            ", stressRampUpSeconds=" + stressRampUpSeconds +
            ", stressDurationSeconds=" + stressDurationSeconds +
            ", browserThreads=" + browserThreads +
            ", browserRampUpSeconds=" + browserRampUpSeconds +
            ", browserDurationSeconds=" + browserDurationSeconds +
            ", maxAverageResponseTime=" + maxAverageResponseTime +
            ", maxResponseTime=" + maxResponseTime +
            ", maxErrorRate=" + maxErrorRate +
            ", minThroughput=" + minThroughput +
            ", maxMemoryUsage=" + maxMemoryUsage +
            ", maxCpuUsage=" + maxCpuUsage +
            '}';
    }
} 
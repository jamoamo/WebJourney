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
 * Data class containing performance metrics collected during test execution.
 *
 * @author James Amoore
 */
public class PerformanceMetrics
{
    // Collection metadata
    private long collectionDuration;
    private int dataPoints;
    
    // CPU metrics
    private double averageCpuUsage;
    private double maxCpuUsage;
    private double minCpuUsage;
    
    // Memory metrics
    private double averageMemoryUsage;
    private long maxMemoryUsage;
    private long minMemoryUsage;
    private long memoryGrowth;
    
    // System metrics
    private double systemLoadAverage;
    private int availableProcessors;
    
    public PerformanceMetrics()
    {
        this.availableProcessors = Runtime.getRuntime().availableProcessors();
    }
    
    // Getters and setters
    public long getCollectionDuration()
    {
        return collectionDuration;
    }
    
    public void setCollectionDuration(long collectionDuration)
    {
        this.collectionDuration = collectionDuration;
    }
    
    public int getDataPoints()
    {
        return dataPoints;
    }
    
    public void setDataPoints(int dataPoints)
    {
        this.dataPoints = dataPoints;
    }
    
    public double getAverageCpuUsage()
    {
        return averageCpuUsage;
    }
    
    public void setAverageCpuUsage(double averageCpuUsage)
    {
        this.averageCpuUsage = averageCpuUsage;
    }
    
    public double getMaxCpuUsage()
    {
        return maxCpuUsage;
    }
    
    public void setMaxCpuUsage(double maxCpuUsage)
    {
        this.maxCpuUsage = maxCpuUsage;
    }
    
    public double getMinCpuUsage()
    {
        return minCpuUsage;
    }
    
    public void setMinCpuUsage(double minCpuUsage)
    {
        this.minCpuUsage = minCpuUsage;
    }
    
    public double getAverageMemoryUsage()
    {
        return averageMemoryUsage;
    }
    
    public void setAverageMemoryUsage(double averageMemoryUsage)
    {
        this.averageMemoryUsage = averageMemoryUsage;
    }
    
    public long getMaxMemoryUsage()
    {
        return maxMemoryUsage;
    }
    
    public void setMaxMemoryUsage(long maxMemoryUsage)
    {
        this.maxMemoryUsage = maxMemoryUsage;
    }
    
    public long getMinMemoryUsage()
    {
        return minMemoryUsage;
    }
    
    public void setMinMemoryUsage(long minMemoryUsage)
    {
        this.minMemoryUsage = minMemoryUsage;
    }
    
    public long getMemoryGrowth()
    {
        return memoryGrowth;
    }
    
    public void setMemoryGrowth(long memoryGrowth)
    {
        this.memoryGrowth = memoryGrowth;
    }
    
    public double getSystemLoadAverage()
    {
        return systemLoadAverage;
    }
    
    public void setSystemLoadAverage(double systemLoadAverage)
    {
        this.systemLoadAverage = systemLoadAverage;
    }
    
    public int getAvailableProcessors()
    {
        return availableProcessors;
    }
    
    public void setAvailableProcessors(int availableProcessors)
    {
        this.availableProcessors = availableProcessors;
    }
    
    @Override
    public String toString()
    {
        return "PerformanceMetrics{" +
            "collectionDuration=" + collectionDuration + "ms" +
            ", dataPoints=" + dataPoints +
            ", averageCpuUsage=" + String.format("%.2f%%", averageCpuUsage) +
            ", maxCpuUsage=" + String.format("%.2f%%", maxCpuUsage) +
            ", minCpuUsage=" + String.format("%.2f%%", minCpuUsage) +
            ", averageMemoryUsage=" + String.format("%.2fMB", averageMemoryUsage) +
            ", maxMemoryUsage=" + maxMemoryUsage + "MB" +
            ", minMemoryUsage=" + minMemoryUsage + "MB" +
            ", memoryGrowth=" + memoryGrowth + "MB" +
            ", systemLoadAverage=" + systemLoadAverage +
            ", availableProcessors=" + availableProcessors +
            '}';
    }
} 
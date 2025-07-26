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
package io.github.jamoamo.webjourney.api.web;

import java.time.Duration;
import java.time.Instant;

/**
 * Statistics and metrics for a browser pool.
 * 
 * <p>
 * This class provides comprehensive statistics about the current state and performance of a browser pool,
 * including pool size, utilization rates, and timing information.
 * </p>
 * 
 * @author James Amoore
 * @see IBrowserPool
 * @since 1.1.0
 */
public final class PoolStatistics
{
	private final int currentSize;
	private final int availableBrowsers;
	private final int inUseBrowsers;
	private final int minSize;
	private final int maxSize;
	private final long totalAcquisitions;
	private final long totalReleases;
	private final long failedAcquisitions;
	private final Duration averageAcquisitionTime;
	private final Duration maxAcquisitionTime;
	private final Instant lastAcquisitionTime;
	private final Instant lastReleaseTime;
	private final boolean isShutdown;

	/**
	 * Creates a new PoolStatistics instance.
	 * 
	 * @param currentSize           The current number of browsers in the pool
	 * @param availableBrowsers     The number of browsers currently available for acquisition
	 * @param inUseBrowsers         The number of browsers currently in use
	 * @param minSize               The minimum pool size
	 * @param maxSize               The maximum pool size
	 * @param totalAcquisitions     The total number of successful browser acquisitions
	 * @param totalReleases         The total number of browser releases
	 * @param failedAcquisitions    The total number of failed browser acquisitions
	 * @param averageAcquisitionTime The average time to acquire a browser
	 * @param maxAcquisitionTime    The maximum time taken to acquire a browser
	 * @param lastAcquisitionTime   The timestamp of the last browser acquisition
	 * @param lastReleaseTime       The timestamp of the last browser release
	 * @param isShutdown            Whether the pool has been shut down
	 */
	public PoolStatistics(int currentSize, int availableBrowsers, int inUseBrowsers, int minSize, int maxSize,
			long totalAcquisitions, long totalReleases, long failedAcquisitions, Duration averageAcquisitionTime,
			Duration maxAcquisitionTime, Instant lastAcquisitionTime, Instant lastReleaseTime, boolean isShutdown)
	{
		this.currentSize = currentSize;
		this.availableBrowsers = availableBrowsers;
		this.inUseBrowsers = inUseBrowsers;
		this.minSize = minSize;
		this.maxSize = maxSize;
		this.totalAcquisitions = totalAcquisitions;
		this.totalReleases = totalReleases;
		this.failedAcquisitions = failedAcquisitions;
		this.averageAcquisitionTime = averageAcquisitionTime;
		this.maxAcquisitionTime = maxAcquisitionTime;
		this.lastAcquisitionTime = lastAcquisitionTime;
		this.lastReleaseTime = lastReleaseTime;
		this.isShutdown = isShutdown;
	}

	/**
	 * Returns the current number of browsers in the pool.
	 * 
	 * @return The current pool size
	 * @since 1.1.0
	 */
	public int getCurrentSize()
	{
		return this.currentSize;
	}

	/**
	 * Returns the number of browsers currently available for acquisition.
	 * 
	 * @return The number of available browsers
	 * @since 1.1.0
	 */
	public int getAvailableBrowsers()
	{
		return this.availableBrowsers;
	}

	/**
	 * Returns the number of browsers currently in use.
	 * 
	 * @return The number of browsers in use
	 * @since 1.1.0
	 */
	public int getInUseBrowsers()
	{
		return this.inUseBrowsers;
	}

	/**
	 * Returns the minimum pool size.
	 * 
	 * @return The minimum pool size
	 * @since 1.1.0
	 */
	public int getMinSize()
	{
		return this.minSize;
	}

	/**
	 * Returns the maximum pool size.
	 * 
	 * @return The maximum pool size
	 * @since 1.1.0
	 */
	public int getMaxSize()
	{
		return this.maxSize;
	}

	/**
	 * Returns the total number of successful browser acquisitions.
	 * 
	 * @return The total number of acquisitions
	 * @since 1.1.0
	 */
	public long getTotalAcquisitions()
	{
		return this.totalAcquisitions;
	}

	/**
	 * Returns the total number of browser releases.
	 * 
	 * @return The total number of releases
	 * @since 1.1.0
	 */
	public long getTotalReleases()
	{
		return this.totalReleases;
	}

	/**
	 * Returns the total number of failed browser acquisitions.
	 * 
	 * @return The total number of failed acquisitions
	 * @since 1.1.0
	 */
	public long getFailedAcquisitions()
	{
		return this.failedAcquisitions;
	}

	/**
	 * Returns the average time to acquire a browser.
	 * 
	 * @return The average acquisition time
	 * @since 1.1.0
	 */
	public Duration getAverageAcquisitionTime()
	{
		return this.averageAcquisitionTime;
	}

	/**
	 * Returns the maximum time taken to acquire a browser.
	 * 
	 * @return The maximum acquisition time
	 * @since 1.1.0
	 */
	public Duration getMaxAcquisitionTime()
	{
		return this.maxAcquisitionTime;
	}

	/**
	 * Returns the timestamp of the last browser acquisition.
	 * 
	 * @return The last acquisition timestamp, or null if no acquisitions have occurred
	 * @since 1.1.0
	 */
	public Instant getLastAcquisitionTime()
	{
		return this.lastAcquisitionTime;
	}

	/**
	 * Returns the timestamp of the last browser release.
	 * 
	 * @return The last release timestamp, or null if no releases have occurred
	 * @since 1.1.0
	 */
	public Instant getLastReleaseTime()
	{
		return this.lastReleaseTime;
	}

	/**
	 * Returns whether the pool has been shut down.
	 * 
	 * @return true if the pool has been shut down
	 * @since 1.1.0
	 */
	public boolean isShutdown()
	{
		return this.isShutdown;
	}

	/**
	 * Returns the pool utilization rate as a percentage.
	 * 
	 * @return The utilization rate (0.0 to 1.0)
	 * @since 1.1.0
	 */
	public double getUtilizationRate()
	{
		if (this.maxSize == 0)
		{
			return 0.0;
		}
		return (double) this.inUseBrowsers / this.maxSize;
	}

	/**
	 * Returns the pool availability rate as a percentage.
	 * 
	 * @return The availability rate (0.0 to 1.0)
	 * @since 1.1.0
	 */
	public double getAvailabilityRate()
	{
		if (this.maxSize == 0)
		{
			return 0.0;
		}
		return (double) this.availableBrowsers / this.maxSize;
	}

	/**
	 * Returns the acquisition success rate as a percentage.
	 * 
	 * @return The success rate (0.0 to 1.0)
	 * @since 1.1.0
	 */
	public double getAcquisitionSuccessRate()
	{
		long totalAttempts = this.totalAcquisitions + this.failedAcquisitions;
		if (totalAttempts == 0)
		{
			return 1.0;
		}
		return (double) this.totalAcquisitions / totalAttempts;
	}

	@Override
	public String toString()
	{
		return String.format(
				"PoolStatistics{currentSize=%d, available=%d, inUse=%d, minSize=%d, maxSize=%d, "
						+ "totalAcquisitions=%d, totalReleases=%d, failedAcquisitions=%d, "
						+ "avgAcquisitionTime=%s, maxAcquisitionTime=%s, utilizationRate=%.2f, "
						+ "availabilityRate=%.2f, successRate=%.2f, isShutdown=%s}",
				this.currentSize, this.availableBrowsers, this.inUseBrowsers, this.minSize, this.maxSize,
				this.totalAcquisitions, this.totalReleases, this.failedAcquisitions,
				this.averageAcquisitionTime, this.maxAcquisitionTime, getUtilizationRate(),
				getAvailabilityRate(), getAcquisitionSuccessRate(), this.isShutdown);
	}
} 

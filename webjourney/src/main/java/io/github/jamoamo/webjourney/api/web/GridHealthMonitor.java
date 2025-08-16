/*
 * The MIT License
 *
 * Copyright 2023 James Amoore.
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

import io.github.jamoamo.webjourney.reserved.selenium.HubConnectionUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of IGridHealthMonitor.
 * Provides continuous monitoring of Selenium Grid hubs with configurable
 * intervals and listener notification.
 * 
 * @author James Amoore
 */
public class GridHealthMonitor implements IGridHealthMonitor
{
	private static final Logger LOGGER = LoggerFactory.getLogger(GridHealthMonitor.class);
	private static final Duration DEFAULT_MONITORING_INTERVAL = Duration.ofSeconds(30);
	
	private final Set<String> monitoredHubs = ConcurrentHashMap.newKeySet();
	private final Map<String, GridStatus> hubStatuses = new ConcurrentHashMap<>();
	private final Set<GridHealthListener> listeners = new CopyOnWriteArraySet<>();
	
	private ScheduledExecutorService scheduler;
	private volatile boolean monitoring = false;
	private Duration monitoringInterval = DEFAULT_MONITORING_INTERVAL;
	
	/**
	 * Creates a new grid health monitor.
	 */
	public GridHealthMonitor()
	{
		// Default constructor
	}
	
	@Override
	public boolean isHubAvailable(String hubUrl)
	{
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			return false;
		}
		
		// Check cached status first
		GridStatus cachedStatus = hubStatuses.get(hubUrl);
		if (cachedStatus != null && !cachedStatus.isStale(Duration.ofMinutes(1)))
		{
			return cachedStatus.isAvailable();
		}
		
		// Perform fresh health check
		return HubConnectionUtils.isHubAvailable(hubUrl);
	}
	
	@Override
	public GridStatus getHubStatus(String hubUrl)
	{
		if (hubUrl == null || hubUrl.trim().isEmpty())
		{
			return new GridStatus(hubUrl, Instant.now(), "Invalid hub URL", 1);
		}
		
		// Return cached status if available and fresh
		GridStatus cachedStatus = hubStatuses.get(hubUrl);
		if (cachedStatus != null && !cachedStatus.isStale(Duration.ofMinutes(1)))
		{
			return cachedStatus;
		}
		
		// Perform fresh status check
		return checkHubStatus(hubUrl);
	}
	
	@Override
	public void registerHealthListener(GridHealthListener listener)
	{
		if (listener != null)
		{
			listeners.add(listener);
		}
	}
	
	@Override
	public void unregisterHealthListener(GridHealthListener listener)
	{
		if (listener != null)
		{
			listeners.remove(listener);
		}
	}
	
	@Override
	public void startMonitoring()
	{
		startMonitoring(DEFAULT_MONITORING_INTERVAL);
	}
	
	@Override
	public void startMonitoring(Duration interval)
	{
		if (interval == null || interval.isNegative() || interval.isZero())
		{
			throw new IllegalArgumentException("Monitoring interval must be positive");
		}
		
		synchronized (this)
		{
			if (monitoring)
			{
				LOGGER.debug("Monitoring already active, restarting with new interval: {}", interval);
				stopMonitoring();
			}
			
			this.monitoringInterval = interval;
			this.scheduler = Executors.newScheduledThreadPool(1, r -> {
				Thread t = new Thread(r, "GridHealthMonitor");
				t.setDaemon(true);
				return t;
			});
			
			this.monitoring = true;
			
			// Start monitoring task
			scheduler.scheduleAtFixedRate(
				this::performHealthChecks,
				0, // Initial delay
				interval.toSeconds(),
				TimeUnit.SECONDS
			);
			
			LOGGER.info("Started Grid health monitoring with interval: {}", interval);
		}
	}
	
	@Override
	public void stopMonitoring()
	{
		synchronized (this)
		{
			if (!monitoring)
			{
				return;
			}
			
			monitoring = false;
			
			if (scheduler != null)
			{
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
				scheduler = null;
			}
			
			LOGGER.info("Stopped Grid health monitoring");
		}
	}
	
	@Override
	public boolean isMonitoring()
	{
		return monitoring;
	}
	
	@Override
	public void addHub(String hubUrl)
	{
		if (hubUrl != null && !hubUrl.trim().isEmpty())
		{
			monitoredHubs.add(hubUrl.trim());
			LOGGER.debug("Added hub to monitoring: {}", hubUrl);
		}
	}
	
	@Override
	public void removeHub(String hubUrl)
	{
		if (hubUrl != null)
		{
			monitoredHubs.remove(hubUrl.trim());
			hubStatuses.remove(hubUrl.trim());
			LOGGER.debug("Removed hub from monitoring: {}", hubUrl);
		}
	}
	
	/**
	 * Performs health checks on all monitored hubs.
	 */
	private void performHealthChecks()
	{
		for (String hubUrl : monitoredHubs)
		{
			try
			{
				GridStatus previousStatus = hubStatuses.get(hubUrl);
				GridStatus currentStatus = checkHubStatus(hubUrl);
				
				// Update status cache
				hubStatuses.put(hubUrl, currentStatus);
				
				// Notify listeners
				notifyListeners(previousStatus, currentStatus);
			}
			catch (Exception e)
			{
				LOGGER.warn("Health check failed for hub: {}", hubUrl, e);
				notifyListenersOfError(hubUrl, e);
			}
		}
	}
	
	/**
	 * Performs a detailed health check on a specific hub.
	 */
	private GridStatus checkHubStatus(String hubUrl)
	{
		Instant checkTime = Instant.now();
		long startTime = System.currentTimeMillis();
		
		try
		{
			boolean available = HubConnectionUtils.isHubAvailable(hubUrl);
			long responseTime = System.currentTimeMillis() - startTime;
			
			if (available)
			{
				return new GridStatus(hubUrl, responseTime, checkTime);
			}
			else
			{
				GridStatus previousStatus = hubStatuses.get(hubUrl);
				int failures = previousStatus != null ? previousStatus.getConsecutiveFailures() + 1 : 1;
				return new GridStatus(hubUrl, checkTime, "Hub not responding", failures);
			}
		}
		catch (Exception e)
		{
			GridStatus previousStatus = hubStatuses.get(hubUrl);
			int failures = previousStatus != null ? previousStatus.getConsecutiveFailures() + 1 : 1;
			return new GridStatus(hubUrl, checkTime, e.getMessage(), failures);
		}
	}
	
	/**
	 * Notifies listeners of status changes.
	 */
	private void notifyListeners(GridStatus previousStatus, GridStatus currentStatus)
	{
		for (GridHealthListener listener : listeners)
		{
			try
			{
				// Notify of status update
				listener.onStatusUpdate(previousStatus, currentStatus);
				
				// Notify of availability changes
				if (previousStatus != null && previousStatus.isAvailable() != currentStatus.isAvailable())
				{
					if (currentStatus.isAvailable())
					{
						listener.onHubAvailable(currentStatus);
					}
					else
					{
						listener.onHubUnavailable(currentStatus);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("Health listener notification failed", e);
			}
		}
	}
	
	/**
	 * Notifies listeners of health check errors.
	 */
	private void notifyListenersOfError(String hubUrl, Exception exception)
	{
		for (GridHealthListener listener : listeners)
		{
			try
			{
				listener.onHealthCheckError(hubUrl, exception);
			}
			catch (Exception e)
			{
				LOGGER.warn("Health listener error notification failed", e);
			}
		}
	}
}

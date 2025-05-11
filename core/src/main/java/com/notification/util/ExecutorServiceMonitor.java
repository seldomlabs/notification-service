package com.notification.util;

import java.util.concurrent.atomic.AtomicLong;

public class ExecutorServiceMonitor
{
	
	InstrumentedScheduledThreadPoolExecutor threadPool;
	
	private AtomicLong totalServiceTime;
	
	private AtomicLong totalPoolTime;
	
	private double numberOfRequestsRetired;
	
	private AtomicLong numberOfRequests;
	
	private AtomicLong aggregateInterRequestArrivalTime;
	
	public double getRequestPerSecondRetirementRate()
	{
		return (double) getNumberOfRequestsRetired() /
		fromNanoToSeconds(threadPool.getAggregateInterRequestArrivalTime());
	}
	
	private double fromNanoToSeconds(AtomicLong aggregateInterRequestArrivalTime)
	{
		return aggregateInterRequestArrivalTime.doubleValue()/10E9;
	}
	
	public double getAverageServiceTime()
	{
		return fromNanoToSeconds(threadPool.getTotalServiceTime()) /
		(double) getNumberOfRequestsRetired();
	}
	
	public double getAverageTimeWaitingInPool()
	{
		return fromNanoToSeconds(this.threadPool.getTotalPoolTime()) /
		(double) this.getNumberOfRequestsRetired();
	}
	
	public double getAverageResponseTime()
	{
		return this.getAverageServiceTime() +
		this.getAverageTimeWaitingInPool();
	}
	
	public double getEstimatedAverageNumberOfActiveRequests()
	{
		return getRequestPerSecondRetirementRate() * (getAverageServiceTime() +
		getAverageTimeWaitingInPool());
	}
	
	public double getRatioOfDeadTimeToResponseTime()
	{
		double poolTime = this.threadPool.getTotalPoolTime().doubleValue();
		return poolTime /
		(poolTime + threadPool.getTotalServiceTime().doubleValue());
	}
	
	public double v()
	{
		return getEstimatedAverageNumberOfActiveRequests() /
		(double) Runtime.getRuntime().availableProcessors();
	}
	
	public AtomicLong getTotalPoolTime()
	{
		return totalPoolTime;
	}
	
	public void setTotalPoolTime(AtomicLong totalPoolTime)
	{
		this.totalPoolTime = totalPoolTime;
	}
	
	public double getNumberOfRequestsRetired()
	{
		return numberOfRequestsRetired;
	}
	
	public void setNumberOfRequestsRetired(double numberOfRequestsRetired)
	{
		this.numberOfRequestsRetired = numberOfRequestsRetired;
	}
	
}

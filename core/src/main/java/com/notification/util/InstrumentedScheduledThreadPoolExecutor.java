package com.notification.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * From: {@link}http://www.infoq.com/articles/Java-Thread-Pool-Performance-Tuning
 * 
 *
 */
public class InstrumentedScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

	 public InstrumentedScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler)
	{
		super(corePoolSize, threadFactory, handler);
		// TODO Auto-generated constructor stub
	}

	// Keep track of all of the request times
	 private final ConcurrentHashMap<Runnable, Long> timeOfRequest =
	         new ConcurrentHashMap<>();
	 private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
	 private long lastArrivalTime;
	 // other variables are AtomicLongs and AtomicIntegers
	private AtomicLong totalServiceTime;
	private AtomicLong totalPoolTime;
	private AtomicLong numberOfRequestsRetired;
	private AtomicLong numberOfRequests;
	private AtomicLong aggregateInterRequestArrivalTime;

	 @Override
	 protected void beforeExecute(Thread worker, Runnable task) {
	   super.beforeExecute(worker, task);
	   startTime.set(System.nanoTime());
	 }

	 @Override
	 protected void afterExecute(Runnable task, Throwable t) {
	   try {
	     totalServiceTime.addAndGet(System.nanoTime() - startTime.get());
	     totalPoolTime.addAndGet(startTime.get() - timeOfRequest.remove(task));
	     numberOfRequestsRetired.incrementAndGet();
	   } finally {
	     super.afterExecute(task, t);
	   }
	 }

	 @Override
	 public void execute(Runnable task) {
	   long now = System.nanoTime();

	   numberOfRequests.incrementAndGet();
	   synchronized (this) {
	     if (lastArrivalTime != 0L) {
	       aggregateInterRequestArrivalTime.addAndGet(now - lastArrivalTime);
	     }
	     lastArrivalTime = now;
	     timeOfRequest.put(task, now);
	   }
	   super.execute(task);
	  }

	
	public long getLastArrivalTime()
	{
		return lastArrivalTime;
	}

	
	public void setLastArrivalTime(long lastArrivalTime)
	{
		this.lastArrivalTime = lastArrivalTime;
	}

	
	public AtomicLong getTotalServiceTime()
	{
		return totalServiceTime;
	}

	
	public void setTotalServiceTime(AtomicLong totalServiceTime)
	{
		this.totalServiceTime = totalServiceTime;
	}

	
	public AtomicLong getTotalPoolTime()
	{
		return totalPoolTime;
	}

	
	public void setTotalPoolTime(AtomicLong totalPoolTime)
	{
		this.totalPoolTime = totalPoolTime;
	}

	
	public AtomicLong getNumberOfRequestsRetired()
	{
		return numberOfRequestsRetired;
	}

	
	public void setNumberOfRequestsRetired(AtomicLong numberOfRequestsRetired)
	{
		this.numberOfRequestsRetired = numberOfRequestsRetired;
	}

	
	public AtomicLong getNumberOfRequests()
	{
		return numberOfRequests;
	}

	
	public void setNumberOfRequests(AtomicLong numberOfRequests)
	{
		this.numberOfRequests = numberOfRequests;
	}

	
	public AtomicLong getAggregateInterRequestArrivalTime()
	{
		return aggregateInterRequestArrivalTime;
	}

	
	public void setAggregateInterRequestArrivalTime(AtomicLong aggregateInterRequestArrivalTime)
	{
		this.aggregateInterRequestArrivalTime = aggregateInterRequestArrivalTime;
	}

	
	public ConcurrentHashMap<Runnable, Long> getTimeOfRequest()
	{
		return timeOfRequest;
	}

	
	public ThreadLocal<Long> getStartTime()
	{
		return startTime;
	}
	 }

package com.notification.util;

/**
 * @author nikhil
 *
 */
public class ThrottleBarrier
{
	private volatile long currentQps;
	private volatile long lastRefreshTime;
	private volatile long numPending;
	
	public final long desiredQps;
	
	public ThrottleBarrier(long desiredQps)
	{
		this.numPending = 0;
		this.desiredQps = desiredQps;
		this.lastRefreshTime = System.currentTimeMillis();
	}
	
	public double acquire() throws InterruptedException
	{	
		boolean refresh;
		long currentTime;
		long waitTime = 0;
		
		synchronized(this)
		{
			currentTime = System.currentTimeMillis();
			
			refresh =  currentTime - this.lastRefreshTime > 1000;
			this.lastRefreshTime = refresh ? currentTime : lastRefreshTime;
			this.currentQps = refresh ? 1 : this.currentQps + 1;
		}
		
		if(this.currentQps > this.desiredQps)
		{
			synchronized(this)
			{												
				waitTime = 1000 + 1000*Math.floorDiv(this.numPending++, this.desiredQps);
			}

			Thread.sleep(waitTime);	
			this.numPending--;
		}
		
		return waitTime;
	}
}
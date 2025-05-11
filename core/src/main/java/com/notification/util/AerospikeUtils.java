package com.notification.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.AerospikeException;

/**
 * 
 * @author abhishek
 *
 */

public class AerospikeUtils
{
	
	private static final Logger logger = LogManager.getLogger(AerospikeUtils.class);
	
	private static final String AEROSPIKE_URL = ApplicationProperties.getInstance()
	.getProperty("aerospike", "url", "");
	
	private static final String AEROSPIKE_PORT = ApplicationProperties.getInstance()
	.getProperty("aerospike", "port", "");
	
	public static final String DEFAULT_NAMESPACE = "";
	
	public static final AerospikeClient aerospikeClient = getAerospikeClient(AEROSPIKE_URL, Integer.valueOf(AEROSPIKE_PORT));
	
	public static AerospikeClient getAerospikeClient(String url, int port)
	{
		logger.info("Fetching Aerospike Client");
		try
		{
			if (aerospikeClient == null || !aerospikeClient.isConnected())
			{
				return new AerospikeClient(url, port);
			}
			else
			{
				return aerospikeClient;
			}
		}
		catch (AerospikeException e)
		{
            logger.error("Aerospike connection not established", e);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		
	}
}

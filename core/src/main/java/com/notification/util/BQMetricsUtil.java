package com.notification.util;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Use this class to throw metrics from the code pieces to BQ
 */
public class BQMetricsUtil {

	static final Logger log = LogManager.getLogger(BQMetricsUtil.class);

	public static final Boolean ENABLE_BQ_METRICS = Boolean.parseBoolean(ApplicationProperties.getInstance().getProperty("aryan", "aryan.metric.BQMetric.enable", "false"));

	static ExecutorService executorService = Executors.newFixedThreadPool(10);

	static String hostName = "";
	static String hostAddress = "";

	static {
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("InetAddress.getLocalHost() not available ");
		}
	}

	public static void publishCountMetric(String metricName, long count, String... tag) {

		if(ENABLE_BQ_METRICS ==  null || ENABLE_BQ_METRICS == false){
			return;
		}
		executorService.submit(() ->
		{
			try{

				List<Map<String, Object>> records = new ArrayList<>();
				Map<String, Object> metricRecord = new HashMap<>();
				metricRecord.put("metric_name", metricName);
				metricRecord.put("metric_time", getCurrentTimestamp());
				metricRecord.put("host", hostAddress);
				metricRecord.put("host_name", hostName);
				metricRecord.put("count", count);
				//metricRecord.put("tag", tag);
				records.add(metricRecord);

				GoogleBigQueryUtils.insertDataIntoBigquery("monitoring", "metrics", records);
			}catch (RuntimeException e){
				log.warn("exception while pushing metric to BigQuery"+ e.getMessage());
			}
		});

	}

	private static String getCurrentTimestamp(){
		Long epochTimeStamp = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(epochTimeStamp);

	}
}

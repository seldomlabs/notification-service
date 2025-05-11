package com.notification.common.constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadConstants {

	private static ThreadPoolExecutor getDefaultFixed(int fixedSize) {
		return (ThreadPoolExecutor) Executors.newFixedThreadPool(fixedSize);
	}

	public static final ThreadPoolExecutor AEROSPIKE_BATCH_EXECUTOR = getDefaultFixed(30);

	public static final ThreadPoolExecutor COUPON_METRICS_PUSH = getDefaultFixed(20);
}

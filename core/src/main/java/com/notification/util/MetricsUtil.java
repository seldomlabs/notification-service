package com.notification.util;

import com.notification.common.ControllerAuditService;
import com.notification.common.constants.ThreadConstants;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Use this class to throw metrics from the code pieces
 */
public class MetricsUtil {

	static Logger logger = LogManager.getLogger(MetricsUtil.class);

	private static final String EXCEPTION = "EXCEPTION";

	private static final String SUCCESS_STATUS_CODE = "200";

	private static final String FAILURE_STATUS_CDOE = "400";

	private static final String EXCEPTION_STATUS_CDOE = "500";

	private static final String delimiterForMetric = ApplicationProperties.getInstance().getProperty("externalsystems", "delimiter_for_metric", "[.]");
	
	public static void publishTimeMetric(String metricName, long timeInMs) {
		BQMetricsUtil.publishCountMetric(metricName, timeInMs);
		double duration = (double) timeInMs;
		duration = duration / 1000;
		ControllerAuditService.requestLatencyHistogram.labels(metricName).observe(duration);
	}

	public static void publishTimeMetricForCoupon(String metricName, long timeInMs) {
		ThreadConstants.COUPON_METRICS_PUSH.submit(() -> {
			double duration = (double) timeInMs;
			duration = duration / 1000;
			ControllerAuditService.requestLatencyHistogram.labels(metricName).observe(duration);
		});
	}
	
	public static void publishTimeMetric(String metricName, long timeInMs, String... tags) {
		BQMetricsUtil.publishCountMetric(metricName, timeInMs, tags);
	}
	
	public static void publishCountMetric(String metricName, String... tags) {
		BQMetricsUtil.publishCountMetric(metricName, 1, tags);
	}
	
	public static void publishCountMetric(String metricName, long count) {
		BQMetricsUtil.publishCountMetric(metricName, count);
	}

	public static void publishCountMetricWithPrometheus(String metricName, String... tags) {
		BQMetricsUtil.publishCountMetric(metricName, 1, tags);
		ControllerAuditService.requests.labels(metricName).inc();
	}

	public static void publishCountMetricWithReason(String metricName, String reason, String... tags) {
		BQMetricsUtil.publishCountMetric(metricName, 1, tags);
		ControllerAuditService.reasonRequests.labels(metricName,reason).inc();
	}
}

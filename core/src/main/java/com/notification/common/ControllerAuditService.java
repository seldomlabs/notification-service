
package com.notification.common;

import com.notification.common.db.service.PropertiesService;
import com.notification.util.ApplicationProperties;
import com.notification.util.MetricsUtil;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * This class contains all the aspects which will be applied on controller class
 * methods.
 *
 * @author Abhishek
 */

@Aspect
public class ControllerAuditService {

    public static final String AUTHENTICATION_HEADER = "Authorization";

    public static final String TRUE = "TRUE";

    final static String FINAL_THRESHOLD_LIMIT = ApplicationProperties.getInstance().getProperty("monitor", "MAX_CALL_TIME", "3000");


    Logger logger = LogManager.getLogger(ControllerAuditService.class);

    public static final String REQUESTS_METRIC_NAME = ApplicationProperties.getInstance().getProperty("aryan", "requests_metric_name", "aryan_common_request");

    public static final String REASONS_REQUESTS_METRIC_NAME = ApplicationProperties.getInstance().getProperty("aryan", "reasons_requests_metric_name", "aryan_common_request_reason");

    public static final String RESPONSE_COUNTER_PM = ApplicationProperties.getInstance().getProperty("aryan", "response_counter_pm", "aryan_http_requests_total");

    public static final String LATENCY_HISTOGRAM = ApplicationProperties.getInstance().getProperty("aryan", "coupon_request_latency_seconds", "coupon_histogram_requests_latency_seconds");

    static double[] bucketArray = {0.02, 0.05, 0.08, 0.1, 0.2, 0.3, 0.5, 0.8, 1.0};

    public static final Counter requests = Counter.build()
            .name(REQUESTS_METRIC_NAME).help("Total requests.").labelNames("path").register();

    public static final Counter reasonRequests = Counter.build()
            .name(REASONS_REQUESTS_METRIC_NAME).help("Total requests.").labelNames("path", "reason").register();

    public static final Histogram requestLatencyHistogram = getRequestLatencyHistogram();

    @Autowired
    BasicAuthenticationService basicAuthenticationService;

    @Autowired
    PropertiesService propertiesService;

    /**
     * This pointcut represents any method of controller class with class name
     * ending with literal "Controller"
     */
    @Pointcut("execution(* com..*Controller.*(..))")
    public void anyControllerMethods() {
    }

    /**
     * This pointcut represents the methods of a controller class which is
     * exposed as a public web end point.
     */
    @Pointcut("(anyControllerMethods() || execution(* *.*Controller.*(..))) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void controllerWebEndpoints() {
    }

    @Pointcut("anyControllerMethods() && @annotation(com.aryan.common.BasicAuthEnabled)")
    public void controllerWebEndpointsWithBasicAuthEnabled() {
    }

    @Pointcut("@annotation(com.aryan.common.TimeMetric)")
    public void methodsForTimeMetric() {
    }

    @Around("methodsForTimeMetric()")
    public Object doPublishTimeMetric(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            // IMP: Must call proceed on the join point and return the result
            Object result = pjp.proceed();
            return result;
        } finally {
            long end = System.currentTimeMillis();
            long timeTakenMillis = end - start;
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String metricName = signature.getName();
            Method method = signature.getMethod();
            TimeMetric timeMetric = method.getAnnotation(TimeMetric.class);
            if (timeMetric != null && StringUtils.isNotBlank(timeMetric.metricName())) {
                metricName = timeMetric.metricName();
            }
        }
    }

    /**
     * This method measures the time takes by the controller method which is
     * responsible for service some mapped URL. In case time taken by method
     * exceed the configured threshold value, it will send out email to
     * configured list of people. This method also logs the time in datadog.
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("controllerWebEndpoints()")
    public Object doAudit(ProceedingJoinPoint pjp) throws Throwable {
        String msg = "Begin " + pjp.toShortString();
        long start = System.currentTimeMillis();
        Histogram.Timer histogramRequestTimer = requestLatencyHistogram.labels(pjp.getSignature().getName()).startTimer();
        try {
            return pjp.proceed();
        } catch (Exception e) {
			String methodName = pjp.getSignature().getName();
			MetricsUtil.publishCountMetricWithPrometheus("coupon.exception." + methodName);
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            long timeTakenMillis = end - start;
            String prometheusSwitch = propertiesService.getProperty("SWITCH", "Prometheus", "ON");
            if ("ON".equalsIgnoreCase(prometheusSwitch)) {
                histogramRequestTimer.observeDuration();
            }

            if (timeTakenMillis > Long.parseLong(FINAL_THRESHOLD_LIMIT)) {
                String subject = "Controller Call Overshooted the threshold";

                String body = pjp.getTarget() + ":" + pjp.toShortString() + "took more than threshold time to execute"
                        + timeTakenMillis;

                logger.info(
                        "TECH_ALERT ::: Subject: Controller Call Overshooted the threshold" + subject + " Body: "
                                + body);

            }

            msg = "[Time] " + timeTakenMillis + "ms [Target] " + pjp.getTarget() + " [Origin] " + pjp.toShortString();

            logger.debug(msg);
        }
    }

    // @Before("controllerWebEndpoints() && args(request,..)")
    public void populateEnvironment(JoinPoint jp, HttpServletRequest request) throws Exception {
        // HttpSession session = request.getSession(false);
    }

    @Around("controllerWebEndpointsWithBasicAuthEnabled()")
    public Object authenticateHeader(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = (HttpServletRequest) pjp.getArgs()[0];

        HttpServletResponse response = (HttpServletResponse) pjp.getArgs()[1];

        String authCredentials = request.getHeader(AUTHENTICATION_HEADER);

        boolean authenticationStatus = basicAuthenticationService.authenticate(authCredentials);

        if (authenticationStatus) {
            return pjp.proceed();
        } else {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return null;
        }
    }

    public static Histogram getRequestLatencyHistogram() {

        return Histogram.build()
                .name(LATENCY_HISTOGRAM)
                .help("histogram request latency in seconds")
                .labelNames("path")
                .buckets(bucketArray)
                .register();
    }
}

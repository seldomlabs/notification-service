package com.notification.common.redis;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import jakarta.annotation.PreDestroy;

import com.notification.util.MetricsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Service;

import com.notification.util.ApplicationProperties;

import redis.clients.jedis.*;

/**
 * @author abhishek
 */

@Service("redisService")
public class RedisService implements Closeable {

    Logger logger = LogManager.getLogger(RedisService.class);

    public static final String REDIS_MAX_CONNECTIONS = ApplicationProperties.getInstance().getProperty("redis",
            "redis.maxconnections", "200");

    public static final String REDIS_MAX_IDLE = ApplicationProperties.getInstance().getProperty("redis",
            "redis.maxidle", "20");

    public static final String REDIS_MIN_IDLE = ApplicationProperties.getInstance().getProperty("redis",
            "redis.minidle", "10");

    public static final String REDIS_MAX_WAIT_MS = ApplicationProperties.getInstance().getProperty("redis",
            "redis.maxwaitms", "500");

    public static int resourceFailure = 0;

    private Map<String, JedisPool> jedisMap;

    public RedisService() {
        jedisMap = new HashMap<String, JedisPool>();
    }

    public Jedis getResourceForUrlWithPort(String url, int port, String password) {
        try {
            if (jedisMap.get(url) == null) {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(Integer.parseInt(REDIS_MAX_CONNECTIONS));
                config.setMaxIdle(Integer.parseInt(REDIS_MAX_IDLE));
                config.setMinIdle(Integer.parseInt(REDIS_MIN_IDLE));
                config.setMaxWaitMillis(Integer.parseInt(REDIS_MAX_WAIT_MS));
                JedisPool redisPool = new JedisPool(config, url, port, Integer.parseInt(REDIS_MAX_WAIT_MS));
                jedisMap.put(url, redisPool);
            } else {
                JedisPool redisPool = jedisMap.get(url);
                logger.info("*********redis pool stat **** active conn {} waiter count {} idle count {} for url {}", redisPool.getNumActive(),
                        redisPool.getNumWaiters(), redisPool.getNumIdle(), url);
                if (redisPool.getNumActive() == Integer.parseInt(REDIS_MAX_CONNECTIONS)) {
                    MetricsUtil.publishCountMetric("Redis Max Connection Reached");
                    MetricsUtil.publishCountMetric("Redis Wait Connections", redisPool.getNumWaiters());
                }
            }
            Jedis jedis = jedisMap.get(url).getResource();
            if (StringUtils.isNotEmpty(password)) {
                jedis.auth(password);
            }
            return jedis;
        } catch (Exception e) {
            logger.error("Exception fetching redis resource", e);
            resourceFailure++;
            return null;
        }
    }

    public Set<String> getPatternKeys(String url, int port, String password, String pattern) {
        Set<String> matchingKeys = new HashSet<>();
        try (Jedis jedis = getResourceForUrlWithPort(url, port, password)) {
            ScanParams params = new ScanParams();
            params.match(pattern);
            String nextCursor = "0";
            do {
                ScanResult<String> scanResult = jedis.scan(nextCursor, params);
                List<String> keys = scanResult.getResult();
                nextCursor = scanResult.getCursor();
                matchingKeys.addAll(keys);
            } while (!nextCursor.equals("0"));
        } catch (Exception e) {
            logger.error("Exception fetching redis resource getPatternKeys", e);
        }
        return matchingKeys;
    }


    @PreDestroy
    public void destroy() throws IOException {
        this.close();
    }

    @Override
    public void close() throws IOException {
        for (String key : jedisMap.keySet()) {
            jedisMap.get(key).close();
        }
    }

    @Override
    public void finalize() throws Throwable {
        this.close();
    }
}

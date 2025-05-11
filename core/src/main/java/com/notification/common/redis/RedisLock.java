package com.notification.common.redis;

import com.notification.util.ApplicationProperties;
import com.notification.util.MetricsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.lang.management.ManagementFactory;

public class RedisLock {

    private @Autowired
    RedisService redisService;

    //defaults to m-stormy.
    private static final String LOCK_REDIS_URL = ApplicationProperties.getInstance().getProperty("redis",
            "lock.redis.host", "localhost");

    private static final int LOCK_REDIS_PORT = Integer.parseInt(ApplicationProperties.getInstance().getProperty(
            "redis", "lock.redis.port", "6379"));

    private static final String LOCK_REDIS_PASSWORD = ApplicationProperties.getInstance().getProperty(
            "redis", "lock.redis.password", null);

    private static final String LOCK_ACQUISITION_REDIS_FAILURE_METRIC = "lock.acquisition.redis.failure";
    private static final String LOCK_RELEASE_REDIS_FAILURE_METRIC = "lock.release.redis.failure";
    private static final String GENERIC_LOCK_TEMPLATE = "generic_%s_lock";

    protected String getRedisUrl() {
        return LOCK_REDIS_URL;
    }

    protected Integer getRedisPort() {
        return LOCK_REDIS_PORT;
    }

    protected String getRedisPassword() {
        return LOCK_REDIS_PASSWORD;
    }
    /**
     * Provides the logger for this locking system.
     * For customisation, this function should be overridden.
     * @return
     */
    protected Logger getLogger(){
        return LogManager.getLogger(RedisLock.class);
    }

    /**
     * Create a key for the id being passed.
     * The lock is held against this key.
     *
     * For extending these locks, this function should be overridden.
     * @param id Value which needs to be locked.
     * @return
     */
    protected String createKey(String id){
        return String.format(GENERIC_LOCK_TEMPLATE, id);
    }

    /**
     * Get lock value based on process name and thread id.
     * The value generated should be unique for each key,
     * else the locking behaviour cannot be guaranteed.
     *
     * To customize, this function should be overridden.
     * The constraint should hold at all times.
     *
     * @return String held against the key which is being locked.
     */
    protected String getLockValue() {
        String lockValue = ((Long) (Thread.currentThread().getId())).toString();
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isNotEmpty(processName)) {
            lockValue = String.join("_",processName,lockValue);
        }
        return lockValue;
    }

    /**
     * @param id Value against which lock has to be taken
     * @param value Value to hold in the lock
     * @param expiry Expiry of the lock
     * @param retry Number of Retry attempts
     * @return Boolean object with the below outcomes:
     * <pre>
     *   Boolean.FALSE = Lock couldn't be taken;
     *   Boolean.TRUE  = Lock successfully acquired;
     *   null          = Cannot coonect to Redis Server;
     * </pre>
     */
    public Boolean lock(String id, String value, int expiry, int retry) {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(id))
            return false;
        String key = createKey(id);
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())){
            if (jedis != null) {
                return this.lock(jedis, key, value, expiry, retry);
            }
            this.getLogger().error("redisServer is null in acquiringLock for value "+ value);
            MetricsUtil.publishCountMetricWithPrometheus(LOCK_ACQUISITION_REDIS_FAILURE_METRIC);
            return null;
        } catch (Exception e){
            this.getLogger().error("Exception while fetching redis for key "+key+" ",e);
            return false;
        }
    }

    public Boolean lock(String id, int expiry, int retry) {
        String lockValue = this.getLockValue();
        this.getLogger().debug("lock value for id "+id+" is "+lockValue);
        return this.lock(id, lockValue, expiry, retry);
    }

    private boolean lock(Jedis jedis, String key, String value, int expiry, int retry) {
        try {
            for(int i = 0; i < retry; ++i) {
            	
            	jedis.setnx(key, value);
            	jedis.expire(key,expiry);
            	
                String redisValue = jedis.get(key);
                if (redisValue == null) {
                    this.getLogger().error("Could not lock key "+key+" via redis");
                    Thread.sleep(100L);
                } else if (redisValue.equals(value)) {
                    this.getLogger().info("redis lock value " + redisValue +" for key "+key);
                    return true;
                }
            }
            return false;
        } catch (InterruptedException var8) {
            this.getLogger().error(var8.getMessage(), var8);
            return false;
        }
    }

    public Boolean unlock(String id) {
        if(id == null){
            return true;
        }
        String key = this.createKey(id);
        String value = this.getLockValue();
        try(Jedis jedis = this.redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if(jedis != null) {
                String redisValue = jedis.get(key);
                if (redisValue != null) {
                    if (redisValue.equals(value)) {
                        jedis.del(key);
                    }
                    return true;
                }
                this.getLogger().info("***Redis alert :: Key not found while cleanup :: " + key);
                return true;
            }
            this.getLogger().error("redisServer is null in KeyValueService");
            MetricsUtil.publishCountMetricWithPrometheus(LOCK_RELEASE_REDIS_FAILURE_METRIC);
            return false;
        } catch (Exception e) {
            this.getLogger().error("Error fetching redis", e);
            return false;
        }
    }
}

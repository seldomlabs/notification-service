package com.notification.common.cache;

import com.notification.common.exception.ApplicationException;
import com.notification.common.redis.RedisService;
import com.notification.util.ApplicationProperties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by sahilmohindroo on 24/10/18
 */
public abstract class MemoryStoreRedisListCacheService extends CacheService<String, List<String>> {

    public static final String REDIS_URL = ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.host", "localhost");
    public static final int REDIS_PORT = Integer.parseInt(ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.port", "6174"));
    public static final String REDIS_PASSWORD = ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.password", null);

    Logger logger = LogManager.getLogger(MemoryStoreRedisListCacheService.class);

    @Autowired
    RedisService redisService;

    protected abstract List<String> createCache(String key) throws ApplicationException;

    public void remove(String key) throws ApplicationException {
    	
    	try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.del(key);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
    }

    @Override
    protected List<String> getCache(String key) throws ApplicationException {
        List<String> value = null;
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            value = jedis.lrange(key, 0, 100);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        return value;
    }

    @Override
    protected void putCache(String key, List<String> values) throws ApplicationException{

        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            
            for (String value: values) {
            	jedis.lpush(key, value);
            }
            
            jedis.expire(key, expiry());
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        
    }

}

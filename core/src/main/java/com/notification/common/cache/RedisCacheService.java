package com.notification.common.cache;

import com.notification.common.exception.ApplicationException;
import com.notification.common.redis.RedisService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * Created by sahilmohindroo on 24/10/18
 */
public abstract class RedisCacheService extends CacheService<String, String> {

    Logger logger = LogManager.getLogger(RedisCacheService.class);

    @Autowired
    RedisService redisService;

    protected abstract String createCache(String key) throws ApplicationException;

    protected abstract String getRedisUrl();
    protected abstract int getRedisPort();
    protected abstract String getRedisPassword();

    
    public void remove(String key) throws ApplicationException {
    	try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
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

    protected String getCache(String key) throws ApplicationException {
        String value = null;
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            value = jedis.get(key);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        return value;
    }

    protected void putCache(String key, String value) throws ApplicationException{
        if (value == null) return;
        logger.info("trying to push cache for key - " + key);
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.set(key, value);
            jedis.expire(key, expiry());
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while putting in redis");
        }
    }

    public List<String> getCache(String[] keys) throws ApplicationException {
        if (keys.length == 0)
            return null;

        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            return jedis.mget(keys);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while putting in redis");
        }
    }

    public void updateExpiry(String key) throws ApplicationException {
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                throw new ApplicationException("redis not available");
            }
            jedis.expire(key, expiry());
        } catch (Exception e) {
            throw new ApplicationException("error while fetching from redis");
        }
    }

    public Long getTTL(String key) throws ApplicationException {
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            return jedis.ttl(key);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
    }

    protected void putCacheWithTTL(String key, String value, Integer ttl) throws ApplicationException{
        if (value == null) return;
        logger.info("trying to push cache for key - " + key);
        try(Jedis jedis = redisService.getResourceForUrlWithPort(getRedisUrl(), getRedisPort(), getRedisPassword())) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.set(key, value);
            jedis.expire(key, ttl);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while putting in redis");
        }
    }
}

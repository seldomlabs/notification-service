package com.notification.common.cache;

import com.notification.common.exception.ApplicationException;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/*
 * Created by Shivam
 */
public abstract class MemoryStoreRedisSetCacheService<K,V> extends MemoryStoreRedisAbstractCacheService<K, Collection<V>> {

	@Override
	protected Collection<V> getCache(K key) throws ApplicationException {
		
        Collection<String> values = new HashSet<String>();
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            values = jedis.smembers(createKeyString(key));
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        
        Collection<V> output = new HashSet<V>();
        
        if (values == null)
        	return output;
        
        for (String value: values) {
        	output.add(deSerializeValue(value));
        }
        
        return output;
	}

	@Override
	protected Set<V> createCache(K key) throws ApplicationException {
		return null;
	}
	
	protected void add(K key, V value) throws ApplicationException {
		
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.sadd(createKeyString(key), serializeValue(value));
            jedis.expire(createKeyString(key), expiry());
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
	}
	
	protected void rem(K key, V value) throws ApplicationException {
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            
            jedis.srem(createKeyString(key), serializeValue(value));
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
	}
	
    public V pop(K key) throws ApplicationException {
        if (key == null) return null;

        V value = popCache(key);
        if (value == null){
        	logger.debug("TESTING - RedisSetCacheService pop returned null. Trying to refresh");
            refresh(key);
            value = popCache(key);
        }
        return value;
    }
    
    @Override
    protected void putCache(K key, Collection<V> values) throws ApplicationException{
        if (CollectionUtils.isEmpty(values)) return;
        logger.info("trying to push cache for key - " + key);
        try(Jedis jedis = getJedis()) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            
            for (V value: values) {
            	jedis.sadd(createKeyString(key), serializeValue(value));
            }
            jedis.expire(createKeyString(key), expiry());
        } catch (RuntimeException e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while putting in redis");
        }
    }
	
	public V popCache(K key) throws ApplicationException {
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            
            String value = jedis.spop(createKeyString(key));
            if (value == null) {
            	logger.debug("TESTING - RedisSetCacheService popCache returned null for " + createKeyString(key));
            	return null;
            }
            
            return deSerializeValue(value);
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
	}
	
    protected abstract String serializeValue(V value) throws ApplicationException;

    protected abstract V deSerializeValue(String valueStr) throws ApplicationException;

	public void updateExpiry(K key) throws ApplicationException {
		
        try(Jedis jedis = redisService.getResourceForUrlWithPort(REDIS_URL, REDIS_PORT, REDIS_PASSWORD)) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.expire(createKeyString(key), expiry());
        } catch (Exception e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
	}
}

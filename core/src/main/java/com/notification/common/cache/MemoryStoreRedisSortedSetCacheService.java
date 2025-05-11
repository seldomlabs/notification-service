package com.notification.common.cache;

import com.notification.common.cache.dto.RedisSortedSetCacheValue;
import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.Tuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sahilmohindroo on 24/10/18
 */
public abstract class MemoryStoreRedisSortedSetCacheService<K, V extends RedisSortedSetCacheValue> extends MemoryStoreRedisAbstractCacheService<K, Collection<V>> {

    Logger logger = LogManager.getLogger(MemoryStoreRedisSortedSetCacheService.class);

    @Override
    public Collection<V> get(K key) throws ApplicationException {
        return null;
    }

    @Override
    protected Collection<V> getCache(K key) throws ApplicationException {
        return null;
    }

    public V popMax(K key) throws ApplicationException {
        if (key == null) return null;

        V value = popMaxCache(key);
        if (value == null){
            refresh(key);
            value = popMaxCache(key);
        }
        return value;
    }

	private V popMaxCache(K key) throws ApplicationException {
        String value = null;
        try(Jedis jedis = getJedis()) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            logger.info("popmax for key -" + createKeyString(key));
            Tuple tuple = jedis.zpopmax(createKeyString(key));
            if (tuple != null){
                logger.info("value found -" + tuple.getElement());
                value = tuple.getElement();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        return deSerializeValue(value);
    }

    public V topMax(K key) throws ApplicationException {
    	if (key == null) return null;

    	V value = topMaxCache(key);
    	if (value == null){
            refresh(key);
            value = topMaxCache(key);
        }
        return value;
    }

    private V topMaxCache(K key) throws ApplicationException {

    	String value = null;
        try(Jedis jedis = getJedis()) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }

            ScanParams scanParams = new ScanParams().count(1);
            String cur = ScanParams.SCAN_POINTER_START;
        	ScanResult<Tuple> scanResults = jedis.zscan(createKeyString(key), cur, scanParams);
            List<Tuple> results = scanResults.getResult();
            
            if (!CollectionUtils.isEmpty(results)) {
            	value = results.get(0).getElement();
            }
        } catch (RuntimeException e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while fetching from redis");
        }
        
        return deSerializeValue(value);
	}

    protected void putCache(K key, Collection<V> values) throws ApplicationException{
        if (CollectionUtils.isEmpty(values)) return;
        logger.info("trying to push cache for key - " + key);
        try(Jedis jedis = getJedis()) {
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            Map<String, Double> scoreMembers = new HashMap<>();
            for (V value: values){
                scoreMembers.put(serializeValue(value), value.getScore());
            }
            if (jedis == null) {
                logger.error("redis not available");
                throw new ApplicationException("redis not available");
            }
            jedis.zadd(createKeyString(key), scoreMembers);
            jedis.expire(createKeyString(key), expiry());
        } catch (RuntimeException e) {
            logger.error("Error fetching redis", e);
            throw new ApplicationException("error while putting in redis");
        }
    }

    protected abstract String serializeValue(V value) throws ApplicationException;

    protected abstract V deSerializeValue(String valueStr) throws ApplicationException;

}

package com.notification.common.cache;

import com.notification.common.exception.ApplicationException;
import com.notification.constants.GlobalConstants;
import com.notification.util.ApplicationProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class MemoryStoreMatchmakingRedisCacheService<V> extends MemoryStoreRedisCacheService {

    public static final String REDIS_URL = ApplicationProperties.getInstance().getProperty("redis", "redis.coupon.host", "localhost");
    public static final int REDIS_PORT = Integer.parseInt(ApplicationProperties.getInstance().getProperty("redis", "redis.coupon.port", "6174"));
    public static final String REDIS_PASSWORD = ApplicationProperties.getInstance().getProperty("redis", "redis.coupon.password", null);

    ObjectMapper mapper = GlobalConstants.objectMapper;

    @Override
    protected String createCache(String key) throws ApplicationException {
        return null;
    }

    @Override
    protected String getRedisUrl(){
        return REDIS_URL;
    }

    @Override
    protected int getRedisPort(){
        return REDIS_PORT;
    }

    @Override
    protected String getRedisPassword(){
        return REDIS_PASSWORD;
    }

    @Override
    protected int expiry() {
        return 0;
    }

    abstract protected String getPrefix();

    abstract protected TypeReference<V> getType();

    protected String getRedisKey(String key) {
        return getPrefix() + key;
    }

    @Override
    public void remove(String key) throws ApplicationException {
        super.remove(getRedisKey(key));
    }

    public V getValue(String parameter) throws ApplicationException {

        V data = null;
        String value = get(getRedisKey(parameter));
        if (value == null)
            return data;
        try {
            data = mapper.readValue(value, getType());
        } catch (IOException e) {
            throw new ApplicationException("Exception while parsing Redis value", e);
        }

        return data;
    }

    public V getCacheValue(String parameter) throws ApplicationException {

        V data = null;
        String value = getCache(getRedisKey(parameter));
        if (value == null)
            return data;
        try {
            data = mapper.readValue(value, getType());
        } catch (IOException e) {
            throw new ApplicationException("Exception while parsing Redis value", e);
        }

        return data;
    }

    public List<V> getCacheValue(List<String> parameters) throws ApplicationException {
        if (CollectionUtils.isEmpty(parameters)) return null;

        String [] keys = new String[parameters.size()];
        int index = 0;
        for (String parameter : parameters)
            keys[index++] = getRedisKey(parameter);

        List<String> vals = getCache(keys);
        if(CollectionUtils.isEmpty(vals)) return null;

        List<V> res = new ArrayList<>();
        for(String val:vals){
            try {
                if(!StringUtils.isEmpty(val)) {
                    res.add(mapper.readValue(val, getType()));
                }else{
                    res.add(null);
                }
            } catch (IOException e) {
                throw new ApplicationException("Exception while parsing Redis value", e);
            }
        }
        return res;
    }

    public void putCacheValue(String parameter, V value) throws ApplicationException {
        String key = getRedisKey(parameter);
        try {
            String val = mapper.writeValueAsString(value);
            putCache(key,val);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Exception while putting Redis value", e);
        }

    }

    public void updateExpiry(String parameter) throws ApplicationException {
        String key = getRedisKey(parameter);
        super.updateExpiry(key);
    }

    public Long getKeyTTL(String parameter) throws ApplicationException {
        String key = getRedisKey(parameter);
        return super.getTTL(key);
    }

    public void putCacheValueWithTTL(String parameter, V value, Integer ttl) throws ApplicationException {
        String key = getRedisKey(parameter);
        try {
            String val = mapper.writeValueAsString(value);
            putCacheWithTTL(key, val, ttl);
        } catch (JsonProcessingException e) {
            throw new ApplicationException("Exception while putting Redis value", e);
        }
    }
}

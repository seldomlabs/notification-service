package com.notification.common.cache;

import com.notification.util.ApplicationProperties;

/**
 * Created by sahilmohindroo on 24/10/18
 */
public abstract class MemoryStoreRedisCacheService extends RedisCacheService {

    public static final String REDIS_URL = ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.host", "localhost");
    public static final int REDIS_PORT = Integer.parseInt(ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.port", "6174"));
    public static final String REDIS_PASSWORD = ApplicationProperties.getInstance().getProperty("redis", "redis.memoryStore.password", null);

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

}

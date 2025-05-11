package com.notification.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public abstract class RedissionFactory {

    private static final Logger logger = LogManager.getLogger(RedissionFactory.class);

    public abstract String getRedisUrl();
    public abstract int getRedisPort();
    public abstract String getRedisPassword();
    public abstract RedissonClient redissonClient();


    public  RedissonClient getRedissonClient()
    {
        try
        {
            if (redissonClient() == null || redissonClient().isShutdown()) {
                synchronized (RedissionFactory.class){
                    if(redissonClient() ==null ||redissonClient().isShutdown()) {
                        logger.info("creating Redission Client");
                        Config config = new Config();
                        (config.useSingleServer()).setAddress("redis://" + getRedisUrl() + ":"+getRedisPort()).setPassword(getRedisPassword());
                       // (config.useSingleServer()).setAddress("redis:// m-pg-redis:6379").setPassword("10ca1j0y");
                        return Redisson.create(config);
                    }
                }
            }
            return redissonClient();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Redission Redis connection not established " + e);
        }
        return null;
    }

}

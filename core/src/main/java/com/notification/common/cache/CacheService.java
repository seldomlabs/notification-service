package com.notification.common.cache;

import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public abstract class CacheService<K, V> {

    Logger logger = LogManager.getLogger(CacheService.class);

    protected abstract int expiry();

    public V get(K key) throws ApplicationException {
        if (key == null) return null;

        V value = getCache(key);
        if (value == null){
            value = refresh(key);
        }
        return value;
    }

    public V refresh(K key) throws ApplicationException {
        if (key == null) return null;

        V value = createCache(key);
        putCache(key, value);
        return value;
    }
    
    public abstract void remove(K key) throws ApplicationException;

    protected abstract V getCache(K key) throws ApplicationException;

    protected abstract void putCache(K key, V value) throws ApplicationException;

    protected abstract V createCache(K key) throws ApplicationException;

}
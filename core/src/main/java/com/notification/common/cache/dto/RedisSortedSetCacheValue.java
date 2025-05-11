package com.notification.common.cache.dto;

/**
 * created by: sahilmohindroo
 * date: 2020-01-05 17:56
 */
public class RedisSortedSetCacheValue {

    double score;

    public RedisSortedSetCacheValue() {
    }

    public RedisSortedSetCacheValue(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

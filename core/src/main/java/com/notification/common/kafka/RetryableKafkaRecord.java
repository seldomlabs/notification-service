package com.notification.common.kafka;

import java.io.Serializable;


public class RetryableKafkaRecord implements Serializable {

    private String message;
    private int retryCount;

    public RetryableKafkaRecord(){

    }

    public RetryableKafkaRecord(String message){
        this.message = message;
        this.retryCount = 0;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}

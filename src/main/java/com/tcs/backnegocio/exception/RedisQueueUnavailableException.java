package com.tcs.backnegocio.exception;

import org.springframework.http.HttpStatus;

public class RedisQueueUnavailableException extends BusinessException {

    public RedisQueueUnavailableException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}

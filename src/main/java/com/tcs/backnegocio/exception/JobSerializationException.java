package com.tcs.backnegocio.exception;

import org.springframework.http.HttpStatus;

public class JobSerializationException extends BusinessException {

    public JobSerializationException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}

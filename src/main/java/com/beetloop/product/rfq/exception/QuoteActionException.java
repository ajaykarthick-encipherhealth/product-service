package com.beetloop.product.rfq.exception;

import org.springframework.http.HttpStatus;

public class QuoteActionException extends RuntimeException {

    private final HttpStatus status;

    public QuoteActionException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public QuoteActionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

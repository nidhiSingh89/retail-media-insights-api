package com.dunnhumby.rmn.insights.service;

/** The request is invalid (HTTP 400). */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}

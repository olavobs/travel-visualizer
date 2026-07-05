package com.flightmonitor.domain.exception;

public class PriceRecordNotFoundException extends RuntimeException {

    public PriceRecordNotFoundException(Long id) {
        super("Price record not found: " + id);
    }
}

package com.flightmonitor.domain.model;

public enum Currency {
    BRL, USD, EUR, GBP;

    public String symbol() {
        return switch (this) {
            case BRL -> "R$";
            case USD -> "US$";
            case EUR -> "€";
            case GBP -> "£";
        };
    }
}

package com.flightmonitor.domain.model;

import java.util.Objects;

public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;

    public User(String email, String passwordHash) {
        this(null, email, passwordHash);
    }

    public User(Long id, String email, String passwordHash) {
        Objects.requireNonNull(email, "Email must not be null");
        Objects.requireNonNull(passwordHash, "Password hash must not be null");
        this.id           = id;
        this.email        = email;
        this.passwordHash = passwordHash;
    }

    public Long getId()             { return id; }
    public String getEmail()        { return email; }
    public String getPasswordHash() { return passwordHash; }
}

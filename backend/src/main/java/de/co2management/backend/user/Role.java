package de.co2management.backend.user;

public enum Role {
    ADMIN,
    SUSTAINABILITY_OFFICER,
    MANAGER,
    USER;

    public boolean requiresSite() {
        return this == MANAGER || this == USER;
    }
}

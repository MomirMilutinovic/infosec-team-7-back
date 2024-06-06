package com.lunark.lunark.auth.model;

import org.springframework.security.core.GrantedAuthority;

public enum AccountRole implements GrantedAuthority {
    GUEST("GUEST"),
    HOST("HOST"),
    ADMIN("ADMIN");

    private String name;

    AccountRole(String name) {
        this.name = name;
    }

    public static AccountRole fromString(String value) {
        switch (value) {
            case "GUEST":
                return AccountRole.GUEST;
            case "HOST":
                return AccountRole.HOST;
            case "ADMIN":
                return AccountRole.ADMIN;
        }
        throw new IllegalArgumentException("Invalid string in account role");
    }

    public static AccountRole fromInt(int value) {
        switch (value) {
            case 0:
                return AccountRole.GUEST;
            case 1:
                return AccountRole.HOST;
            case 2:
                return AccountRole.ADMIN;
        }
        throw new IllegalArgumentException("Invalid int in account role");
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}

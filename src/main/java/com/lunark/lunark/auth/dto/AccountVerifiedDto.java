package com.lunark.lunark.auth.dto;

import com.lunark.lunark.auth.model.Account;

import java.util.UUID;

public class AccountVerifiedDto {
    private UUID id;
    private String email;
    private String password;
    private String name;
    private String surname;
    private String address;
    private String phoneNumber;
    private String role;
    private boolean verified;

    public AccountVerifiedDto() {

    }
    public AccountVerifiedDto(UUID id, String email, String password, String name, String surname, String address, String phoneNumber, String role, boolean verified) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.verified = verified;
    }
    public AccountVerifiedDto(Account account) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.name = account.getName();
        this.surname = account.getSurname();
        this.address = account.getAddress();
        this.phoneNumber = account.getPhoneNumber();
        this.role = account.getRole().toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

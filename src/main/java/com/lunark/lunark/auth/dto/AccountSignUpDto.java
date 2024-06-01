package com.lunark.lunark.auth.dto;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class AccountSignUpDto {
    private UUID id;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "The entered email must be a valid email")
    private String email;
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @NotBlank
    @Pattern(message="Name can contain alphanumeric characters only", regexp = "[a-zA-Z0-9 ]+")
    private String name;
    @NotBlank
    @Pattern(message="Surname can contain alphanumeric characters only", regexp = "[a-zA-Z0-9 ]+")
    private String surname;
    @NotBlank
    @Pattern(message="Address can contain alphanumeric characters only", regexp = "[a-zA-Z0-9 ]+")
    private String address;
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(message = "Phone numbers can contain only digits, spaces, pluses, and dashes", regexp = "[- +()0-9]+")
    private String phoneNumber;
    @NotBlank(message = "Role cannot be blank")
    @Pattern(message = "Role must be either 'GUEST' or 'HOST' (without quotes)", regexp = "GUEST|HOST")
    private String role;

    public AccountSignUpDto() {

    }

    public AccountSignUpDto(String email, String password, String name, String surname, String address, String phoneNumber, String role) {
        this.id = null;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public AccountSignUpDto(Account account) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.name = account.getName();
        this.surname = account.getSurname();
        this.address = account.getAddress();
        this.phoneNumber = account.getPhoneNumber();
        this.role = account.getRole().toString();
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Account toAccount() {
        AccountRole accountRole = AccountRole.fromString(this.role);

        return new Account(id, email, name, surname, address, phoneNumber, accountRole, new ArrayList<>(), new HashSet<>());
    }
}

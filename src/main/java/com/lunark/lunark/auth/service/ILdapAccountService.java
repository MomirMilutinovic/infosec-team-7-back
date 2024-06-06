package com.lunark.lunark.auth.service;

import com.lunark.lunark.auth.model.LdapAccount;

import java.util.Optional;
import java.util.UUID;

public interface ILdapAccountService {
    Optional<LdapAccount> find(UUID id);
    LdapAccount update(LdapAccount account);
    void delete(UUID id);
}

package com.lunark.lunark.auth.service;


import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.auth.repository.ILdapAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LdapAccountService implements ILdapAccountService {
    private ILdapAccountRepository ldapAccountRepository;

    @Autowired
    public LdapAccountService(ILdapAccountRepository ldapAccountRepository) {
        this.ldapAccountRepository = ldapAccountRepository;
    }

    @Override
    public Optional<LdapAccount> find(UUID id) {
        return ldapAccountRepository.findByUuid(id);
    }

    @Override
    public LdapAccount update(LdapAccount account) {
        Optional<LdapAccount> accountToUpdate = ldapAccountRepository.findByUuid(account.getUuid());
        if (accountToUpdate.isEmpty()) {
            return null;
        }
        return ldapAccountRepository.save(account);
    }

    @Override
    public void delete(UUID id) {
        ldapAccountRepository.deleteByUuid(id);
    }
}

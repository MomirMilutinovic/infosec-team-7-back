package com.lunark.lunark.auth.repository;

import com.lunark.lunark.auth.model.LdapAccount;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ILdapAccountRepository extends LdapRepository<LdapAccount> {
}

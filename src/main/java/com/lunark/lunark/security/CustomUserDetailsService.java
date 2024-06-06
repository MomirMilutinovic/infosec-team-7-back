package com.lunark.lunark.security;

import com.lunark.lunark.auth.repository.ILdapAccountRepository;
import com.lunark.lunark.auth.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    ILdapAccountRepository ldapAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println(email);
        return ldapAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + email));
    }
}

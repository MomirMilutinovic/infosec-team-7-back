package com.lunark.lunark.auth.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.repository.IAccountRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    @InjectMocks
    private AccountService accountService;

    private Account account = new Account(UUID.randomUUID(), "host@example.com", "Mirna", "Studsluzvic", "Trg Dositeja Obradovica 6, Novi Sad", "021555555", AccountRole.HOST, null, null);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        account = new Account(UUID.randomUUID(), "host@example.com", "Mirna", "Studsluzbic", "Trg Dositeja Obradovica 6, Novi Sad", "021555555", AccountRole.HOST, null, null);
    }

    @Test
    public void testAdd() {
        Mockito.when(mockPasswordEncoder.encode(account.getPassword())).thenReturn(account.getPassword());
        Mockito.when(accountRepository.saveAndFlush(account)).thenReturn(account);
        Assertions.assertSame(account, accountService.create(account));
        Mockito.when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        Assertions.assertSame(account, accountService.find(account.getId()).get());
    }

    @Test
    public void testUpdate() {
        account.setEmail("email");
        Mockito.when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        Mockito.when(accountRepository.saveAndFlush(account)).thenReturn(account);
        Assertions.assertSame(account, accountService.update(account));
    }

    @Test
    public void testDelete() {
        accountService.delete(account.getId());
        Mockito.doReturn(Optional.ofNullable(null)).when(accountRepository).findById(account.getId());
        Assertions.assertTrue(accountService.find(account.getId()).isEmpty());
    }

    @Disabled
    @ParameterizedTest
    @CsvSource(value = {
            "6, true",
            "5, false"
    })
    public void testFindById(int id, boolean found) {
        Mockito.lenient().when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        UUID accountId = UUID.randomUUID();
        Assertions.assertEquals(found, accountService.find(accountId).isPresent());
    }
}

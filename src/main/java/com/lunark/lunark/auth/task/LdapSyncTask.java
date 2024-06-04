package com.lunark.lunark.auth.task;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.auth.repository.ILdapAccountRepository;
import com.lunark.lunark.auth.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class LdapSyncTask {

    @Autowired
    IAccountService accountService;

    @Autowired
    ILdapAccountRepository ldapAccountRepository;

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    @Transactional
    public void syncLdapUsers() {
        System.out.println("SYNCING USERS");
        // Perform LDAP query to get a list of user accounts
        List<LdapAccount> ldapUsers = ldapAccountRepository.findAll();

        // Iterate through LDAP users and synchronize with SQL database
        for (LdapAccount ldapUser : ldapUsers) {
            // Check if the user exists in the SQL database
            Optional<Account> accountOptional = accountService.find(ldapUser.getId());

            if (accountOptional.isEmpty()) {
                accountService.create(ldapUser.toAccount());
            } else {
                Account updatedAccount = ldapUser.toAccount();
                updatedAccount.setReviews(accountOptional.get().getReviews());
                updatedAccount.setFavoriteProperties(accountOptional.get().getFavoriteProperties());
                accountService.updateSql(updatedAccount);
            }
        }

        // Get a list of deleted users by comparing LDAP and SQL user lists
        List<Account> deletedUsers = getDeletedUsers(ldapUsers);

        // Mark deleted users as deleted in the SQL database
        for (Account deletedUser : deletedUsers) {
            accountService.delete(deletedUser.getId());
        }
    }

    private List<Account> getDeletedUsers(List<LdapAccount> ldapUsers) {
        // Get a list of all SQL users
        List<Account> sqlUsers = accountService.findAll().stream().toList();

        // Filter the SQL users to only those that are not in the LDAP user list
        return sqlUsers.stream()
                .filter(account -> ldapUsers.stream()
                        .noneMatch(ldapUser -> ldapUser.getId().equals(account.getId())))
                .toList();
    }
}
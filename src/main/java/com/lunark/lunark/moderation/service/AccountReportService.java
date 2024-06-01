package com.lunark.lunark.moderation.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.service.IAccountService;
import com.lunark.lunark.exceptions.AccountNotFoundException;
import com.lunark.lunark.moderation.model.AccountReport;
import com.lunark.lunark.moderation.repository.IAccountReportRepository;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reservations.service.IReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountReportService implements IAccountReportService {


    @Autowired
    private IAccountReportRepository accountReportRepository;

    @Autowired
    private IAccountService accountService;

    @Autowired
    private IReservationService reservationService;

    @Override
    public Optional<AccountReport> getById(Long id) {
        return accountReportRepository.findById(id);
    }

    @Override
    public List<AccountReport> getAll() {
        return accountReportRepository.findAll();
    }

    @Override
    public AccountReport create(AccountReport report) {
        if (isUnauthorized(report)) {
            throw new RuntimeException("This report can not be made");
        }
        return this.accountReportRepository.saveAndFlush(report);
    }

    private boolean canReportEachOther(Account guest, Account host) {
        return this.accountReportRepository.canReportEachOther(guest.getId(), host.getId());
    }

    private boolean isUnauthorized(AccountReport report) {
        Account reporter = report.getReporter();
        Account reported = report.getReported();
        switch (report.getReporter().getRole()) {
            case GUEST:
                return !this.canReportEachOther(reporter, reported);
            case HOST:
                return !this.canReportEachOther(reported, reporter);
            default:
                return true;
        }
    }

    @Override
    public boolean isGuestEligibleToReport(Account guest, UUID hostId) {
        if (accountService.find(hostId).isEmpty()) {
            throw new AccountNotFoundException("Could not find host with given id");
        }
        if (guest.getRole().equals(AccountRole.GUEST) && this.accountReportRepository.canReportEachOther(guest.getId(), hostId)) {
            return true;
        }
        return false;
    }

    @Override
    public void block(UUID id) {
        Optional<Account> optionalAccount = accountService.find(id);
        if(optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            blockAccount(account);
            cancelAllResevations(reservationService.getAllReservationsForUser(account.getId()));
            removeAccountReportsForBlockedAccount(account);
        } else {
            throw new AccountNotFoundException("Account with the specified id does not exist");
        }
    }

    private void blockAccount(Account account) {
        // TODO: Block account in ldap or keycloak
        accountService.saveAndFlush(account);
    }

    private void cancelAllResevations(List<Reservation> reservationList) {
        accountService.cancelAllReservations(reservationList);
    }

    private void removeAccountReportsForBlockedAccount(Account account) {
        List<AccountReport> reportsToRemove = findReportsByReportedEmail(account.getEmail());
        if (!reportsToRemove.isEmpty()) {
            accountReportRepository.deleteAll(reportsToRemove);
        }
    }

    private List<AccountReport> findReportsByReportedEmail(String email) {
        return accountReportRepository.findByReportedEmail(email);
    }
}

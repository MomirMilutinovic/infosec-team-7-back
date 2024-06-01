package com.lunark.lunark.moderation.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.moderation.model.AccountReport;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountReportService {
    Optional<AccountReport> getById(Long id);
    List<AccountReport> getAll();

    AccountReport create(AccountReport report);

    boolean isGuestEligibleToReport(Account guest, UUID hostId);

    void block(UUID id);
}

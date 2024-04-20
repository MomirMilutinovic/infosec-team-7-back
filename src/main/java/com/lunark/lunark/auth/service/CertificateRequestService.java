package com.lunark.lunark.auth.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.CertificateRequest;
import com.lunark.lunark.auth.repository.ICertificateRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Clock;
import java.time.ZoneId;

@Service
public class CertificateRequestService implements ICertificateRequestService {
    private ICertificateRequestRepository certificateRequestRepository;
    private Clock clock;

    @Autowired
    public CertificateRequestService(ICertificateRequestRepository certificateRequestRepository, Clock clock) {
        this.certificateRequestRepository = certificateRequestRepository;
        this.clock = clock;
    }

    @Override
    public void create(Account account) {
        Date date = Date.valueOf(clock.instant().atZone(ZoneId.systemDefault()).toLocalDate());
        certificateRequestRepository.saveAndFlush(new CertificateRequest(account, date));
    }
}

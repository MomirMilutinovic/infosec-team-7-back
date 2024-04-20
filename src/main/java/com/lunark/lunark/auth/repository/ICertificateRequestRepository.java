package com.lunark.lunark.auth.repository;

import com.lunark.lunark.auth.model.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {
}

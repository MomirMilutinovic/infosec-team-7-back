package com.lunark.lunark.moderation.repository;

import com.lunark.lunark.moderation.model.AccountReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountReportRepository extends JpaRepository<AccountReport, Long> {
    Optional<AccountReport> findById(Long id);
    @Query("SELECT ar FROM AccountReport ar WHERE ar.reported.email = :email")
    List<AccountReport> findByReportedEmail(@Param("email") String email);
    @Query("select count(r) > 0 from Reservation r where r.guest.id = :guest_id and r.property.host.id = :host_id and r.status = 1 and r.endDate < CURRENT_DATE")
    boolean canReportEachOther(@Param("guest_id") UUID guestId, @Param("host_id") UUID hostId);
}

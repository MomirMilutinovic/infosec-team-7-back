package com.lunark.lunark.reservations.repository;

import com.lunark.lunark.reports.model.DailyReport;
import com.lunark.lunark.reports.model.MonthlyReport;
import com.lunark.lunark.reservations.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Query("select r from Reservation r where r.guest.id = :guest_id and r.property.id = :property_id and r.status = 1 and r.endDate between :from and CURRENT_DATE")
    Collection<Reservation> findAllPastReservationsAtPropertyForGuestAfterDate(@Param("guest_id") UUID guestId, @Param("property_id") Long propertyId, @Param("from") LocalDate from);

    Optional<Reservation> findById(Long id);
    List<Reservation> findByPropertyId(Long propertyId);
    @Query("select r from Reservation r where r.property.id = ?1 and ?2 between r.startDate and r.endDate and r.status = 0")
    List<Reservation> findAllPendingReservationsAtPropertyThatContainDate(Long propertyId, LocalDate date);
    @Query("select r from Reservation r where r.guest.id = :guest_id and r.property.host.id = :host_id and r.status = 1 and r.endDate between :from and CURRENT_DATE")
    Collection<Reservation> findAllPastReservationsAtHostAfterDate(@Param("guest_id") UUID guestId, @Param("host_id") UUID hostId, @Param("from") LocalDate from);

    @Query("select new com.lunark.lunark.reports.model.MonthlyReport(extract(month from r.endDate), sum(r.price), count(*)) " +
            "from Reservation r " +
            "where extract(year from r.endDate) = :year " +
            "and r.property.id = :property_id " +
            "and r.endDate < current_date " +
            "and r.status = 1 " +
            "group by extract(month from r.endDate) " +
            "order by extract(month from r.endDate) ")
    Collection<MonthlyReport> getMonthlyReports(@Param("property_id") Long propertyId, @Param("year") Integer year);

    @Query("select new com.lunark.lunark.reports.model.DailyReport(r.endDate, sum(r.price), count(*)) " +
            "from Reservation r " +
            "where r.property.host.id = :host_id " +
            "and r.endDate >= :start_date " +
            "and r.endDate <= :end_date " +
            "and r.endDate < current_date " +
            "and r.status = 1 " +
            "group by r.endDate")
    Collection<DailyReport> getDailyReports(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate, @Param("host_id") UUID hostId);
}

package com.lunark.lunark.reports.service;

import com.lunark.lunark.reports.model.DailyReport;
import com.lunark.lunark.reports.model.GeneralReport;
import com.lunark.lunark.reports.model.MonthlyReport;
import com.lunark.lunark.reports.model.PropertyReport;
import com.lunark.lunark.reservations.repository.IReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

@Service
public class ReportService implements IReportService {
    private final IReservationRepository reservationRepository;

    @Autowired
    public ReportService(IReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public GeneralReport generateGeneralReport(LocalDate start, LocalDate end, UUID hostId) {
        Collection<DailyReport> dailyReports = reservationRepository.getDailyReports(start, end, hostId);
        Double totalProfit = dailyReports.stream().mapToDouble(report -> report.getProfit()).sum();
        Long totalReservationCount = dailyReports.stream().mapToLong(report -> report.getReservationCount()).sum();

        return new GeneralReport(dailyReports, totalProfit, totalReservationCount);
    }

    @Override
    public PropertyReport generatePropertyReport(Integer year, Long propertyId) {
        Collection<MonthlyReport> monthlyReports = reservationRepository.getMonthlyReports(propertyId, year);

        return new PropertyReport(monthlyReports);
    }
}

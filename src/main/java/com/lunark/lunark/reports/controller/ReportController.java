package com.lunark.lunark.reports.controller;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.reports.dto.GeneralReportResponseDto;
import com.lunark.lunark.reports.dto.PropertyReportResponseDto;
import com.lunark.lunark.reports.model.GeneralReport;
import com.lunark.lunark.reports.model.MonthlyReport;
import com.lunark.lunark.reports.model.PropertyReport;
import com.lunark.lunark.reports.service.IReportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;

@RestController
@RequestMapping("api/reports")
public class ReportController {
    private final IReportService reportService;
    private final ModelMapper modelMapper;

    @Autowired
    public ReportController(IReportService reportService, ModelMapper modelMapper) {
        this.reportService = reportService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/general")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<GeneralReportResponseDto> generateGeneralReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
            ) {
        Account account = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();

        GeneralReport result = reportService.generateGeneralReport(start, end, account.getId());

        return new ResponseEntity<>(modelMapper.map(result, GeneralReportResponseDto.class), HttpStatus.OK);
    }

    @GetMapping("/property")
    @PreAuthorize("hasAuthority('HOST')")
    public ResponseEntity<PropertyReportResponseDto> generatePropertyReport(
            @RequestParam Integer year,
            @RequestParam Long propertyId
    ) {
        PropertyReport result = reportService.generatePropertyReport(year, propertyId);

        return new ResponseEntity<>(modelMapper.map(result, PropertyReportResponseDto.class), HttpStatus.OK);
    }
}

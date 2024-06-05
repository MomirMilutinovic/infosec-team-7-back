package com.lunark.lunark.moderation.controller;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.exceptions.AccountNotFoundException;
import com.lunark.lunark.mapper.AccountReportDtoMapper;
import com.lunark.lunark.moderation.dto.AccountReportRequestDto;
import com.lunark.lunark.moderation.dto.AccountReportResponseDto;
import com.lunark.lunark.moderation.dto.HostReportEligibilityDto;
import com.lunark.lunark.moderation.model.AccountReport;
import com.lunark.lunark.moderation.service.IAccountReportService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reports/accounts")
@Validated
public class AccountReportController {
    @Autowired
    private IAccountReportService accountReportService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AccountReportDtoMapper accountReportDtoMapper;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountReportResponseDto> getAccountReport(@PathVariable("id") @PositiveOrZero Long id) {
        return accountReportService.getById(id)
                .map(accountReport -> ResponseEntity.ok(modelMapper.map(accountReport, AccountReportResponseDto.class)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountReportResponseDto>> getAll() {
        List<AccountReport> accountReports = accountReportService.getAll();
        List<AccountReportResponseDto> reportResponseDtos = accountReports.stream()
                .map(report -> accountReportDtoMapper.toDto(report))
                .collect(Collectors.toList());

        return accountReports.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(reportResponseDtos, HttpStatus.OK );
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('GUEST') or hasAuthority('HOST')")
    public ResponseEntity<AccountReportResponseDto> createReport(@Valid @RequestBody AccountReportRequestDto reportRequestDto) throws ConstraintViolationException {
        Account reporter = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        AccountReport report;
        try {
            report = accountReportDtoMapper.toAccountReport(reportRequestDto, reporter);
            report = this.accountReportService.create(report);
            return new ResponseEntity<>(accountReportDtoMapper.toDto(report), HttpStatus.CREATED);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<AccountReportResponseDto> block(@PathVariable @PositiveOrZero Long id) {
        Optional<AccountReport> report = accountReportService.getById(id);
        if (report.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        accountReportService.block(report.get().getReported().getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/host-report-eligibility/{hostId}")
    @PreAuthorize("hasAuthority('GUEST')")
    public ResponseEntity<HostReportEligibilityDto> isCurrentGuestEligibleToReport(@PathVariable("hostId") @NotNull UUID hostId) {
        Account reporter = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        boolean eligible = accountReportService.isGuestEligibleToReport(reporter, hostId);
        return new ResponseEntity<>(new HostReportEligibilityDto(hostId, eligible), HttpStatus.OK);
    }
}

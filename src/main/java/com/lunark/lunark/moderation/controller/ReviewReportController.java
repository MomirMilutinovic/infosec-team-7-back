package com.lunark.lunark.moderation.controller;

import com.lunark.lunark.mapper.ReviewReportDtoMapper;
import com.lunark.lunark.moderation.dto.ReviewReportRequestDto;
import com.lunark.lunark.moderation.dto.ReviewReportResponseDto;
import com.lunark.lunark.moderation.exception.UnauthorizedReportException;
import com.lunark.lunark.moderation.model.ReviewReport;
import com.lunark.lunark.moderation.service.ReviewReportService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reports/reviews")
public class ReviewReportController {
    @Autowired
    private ReviewReportService reviewReportService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ReviewReportDtoMapper reviewReportDtoMapper;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('read_report')")
    public ResponseEntity<ReviewReport> getReviewReport(@PathVariable("id") Long id) {
        ReviewReport reviewReport = reviewReportService.getById(id);
        return reviewReport != null ? new ResponseEntity<>(reviewReport, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('read_report')")
    public ResponseEntity<List<ReviewReportResponseDto>> getAll() {
        List<ReviewReport> reviewReports = reviewReportService.getAll();
        List<ReviewReportResponseDto> reportDtos = reviewReports.stream()
                .map(report -> new ReviewReportResponseDto(report.getId(), report.getDate(), report.getReporter().getId(), report.getReview().getId()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(reportDtos, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('report_review')")
    public ResponseEntity<ReviewReportResponseDto> createReviewReport(@RequestBody ReviewReportRequestDto dto) {
        Optional<ReviewReport> reviewReport = reviewReportDtoMapper.toReviewReport(dto);
        if (reviewReport.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        try {
            ReviewReport savedReport = this.reviewReportService.create(reviewReport.get());
            return new ResponseEntity<>(modelMapper.map(savedReport, ReviewReportResponseDto.class), HttpStatus.CREATED);
        } catch (UnauthorizedReportException ex) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (DataIntegrityViolationException ex) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('delete_report')")
    public ResponseEntity<Void> deleteReviewReport(@PathVariable Long id) {
        reviewReportService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

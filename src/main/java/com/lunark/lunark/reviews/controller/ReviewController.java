package com.lunark.lunark.reviews.controller;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.mapper.ReviewDtoMapper;
import com.lunark.lunark.reviews.dto.*;
import com.lunark.lunark.reviews.model.Review;
import com.lunark.lunark.reviews.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/reviews")
@Validated
public class ReviewController {

    @Autowired
    ReviewService reviewService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    Clock clock;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReviewDto> getReview(@PathVariable("id") @NotNull @PositiveOrZero Long id) {
        Optional<Review> review = reviewService.find(id);
        if (review.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<ReviewDto>(ReviewDtoMapper.toDto(review.get()), HttpStatus.OK);
    }

    @GetMapping(value = "/host/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ReviewDto>> getReviewForHost(@PathVariable("id") @NotNull UUID id) {
        Collection<Review> reviews = reviewService.getAllReviewsForHost(id);
        if(reviews == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reviews.stream().map(ReviewDtoMapper::toDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @GetMapping(value = "/property/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<ReviewDto>> getReviewForProperty(@PathVariable("id") @NotNull @PositiveOrZero Long id) {
        Collection<Review> reviews = reviewService.getALlReviewsForProperty(id);
        if(reviews == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reviews.stream().map(ReviewDtoMapper::toDto).collect(Collectors.toList()), HttpStatus.OK);
    }

    @PostMapping(value = "/property/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_review')")
    public ResponseEntity<ReviewDto> createPropertyReview(@Valid @RequestBody ReviewRequestDto reviewDto, @PathVariable(value = "id") @NotNull @PositiveOrZero Long id) {
        Account guest = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!this.reviewService.guestEligibleToReviewProperty(guest.getId(), id)) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        Review review = ReviewDtoMapper.toPropertyReview(reviewDto, guest);
        review = this.reviewService.createPropertyReview(review, id);
        return new ResponseEntity<>(ReviewDtoMapper.toDto(review), HttpStatus.OK);
    }

    @PostMapping(value = "/host/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_review')")
    public ResponseEntity<ReviewDto> createHostReview(@Valid @RequestBody ReviewRequestDto reviewDto, @PathVariable(value = "id") @NotNull UUID id) {
        Account guest = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!this.reviewService.guestEligibleToReviewHost(guest.getId(), id)) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
        Review review = ReviewDtoMapper.toHostReview(reviewDto, guest);
        review = this.reviewService.createHostReview(review, id);
        return new ResponseEntity<>(ReviewDtoMapper.toDto(review), HttpStatus.OK);
    }


    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('write_review')")
    public ResponseEntity<Review> deleteReview(@PathVariable("id") @NotNull @PositiveOrZero Long id){
        Account guest = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (reviewService.find(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!reviewService.isBy(guest, id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        reviewService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('write_review')")
    public ResponseEntity<Review> updateReview(@Valid @RequestBody ReviewDto reviewDto, @PathVariable("id") @NotNull @PositiveOrZero Long id) {
        Optional<Review> existingReview = reviewService.find(id);
        if(existingReview.isEmpty()) {
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            Review updatedReview = reviewService.update(existingReview.get());
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path="/unapproved", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('read_unapproved_reviews')")
    public ResponseEntity<List<ReviewDto>> getUnapprovedReviews(SpringDataWebProperties.Pageable pageable) {
        List<Review> unapprovedReviews = reviewService.findAllUnapproved().stream().toList();
        List<ReviewDto> reviewDtos = unapprovedReviews.stream().map(ReviewDtoMapper::toDto).toList();
        return new ResponseEntity<>(reviewDtos, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/approve", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('approve_review')")
    public ResponseEntity<ReviewApprovalDto> approveReview(@PathVariable("id") @NotNull @PositiveOrZero Long id) {
        Optional<Review> existingReview = reviewService.find(id);
        if(existingReview.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            Review approvedReview = this.reviewService.approveReview(existingReview.get());
            return new ResponseEntity<>(modelMapper.map(approvedReview, ReviewApprovalDto.class), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "property-review-eligibility/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PropertyReviewEligibilityDto> checkEligibilityToReviewProperty(@PathVariable("id") @NotNull @PositiveOrZero Long id) {
        Account guest;
        try {
            guest = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new PropertyReviewEligibilityDto(false, null, id), HttpStatus.OK);
        }
        PropertyReviewEligibilityDto propertyReviewEligibilityDto = new PropertyReviewEligibilityDto(reviewService.guestEligibleToReviewProperty(guest.getId(), id), guest.getId(), id);
        return new ResponseEntity<>(propertyReviewEligibilityDto, HttpStatus.OK);
    }

    @GetMapping(value = "host-review-eligibility/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HostReviewEligibilityDto> checkEligibilityToReviewHost(@PathVariable("id") @NotNull UUID id) {
        Account guest;
        try {
            guest = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        } catch (ClassCastException e) {
            return new ResponseEntity<>(new HostReviewEligibilityDto(false, null, id), HttpStatus.OK);
        }
        HostReviewEligibilityDto hostReviewEligibilityDto = new HostReviewEligibilityDto(reviewService.guestEligibleToReviewHost(guest.getId(), id), guest.getId(), id);
        return new ResponseEntity<>(hostReviewEligibilityDto, HttpStatus.OK);
    }

}

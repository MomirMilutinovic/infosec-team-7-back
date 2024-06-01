package com.lunark.lunark.reviews.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.service.IAccountService;
import com.lunark.lunark.notifications.service.INotificationService;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.properties.repostiory.IPropertyRepository;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reservations.repository.IReservationRepository;
import com.lunark.lunark.reviews.model.Review;
import com.lunark.lunark.reviews.repository.IReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewService implements IReviewService<Review> {
    private static final int reviewDeadline = 7;

    @Autowired
    IReviewRepository reviewRepository;

    @Autowired
    IAccountService accountService;

    @Autowired
    IPropertyRepository propertyRepository;

    @Autowired
    IReservationRepository reservationRepository;

    @Autowired
    INotificationService notificationService;

    @Autowired
    Clock clock;

    @Override
    public Collection<Review> findAll() {
        return reviewRepository.findAll();
    }

    @Override
    public Optional<Review> find(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Review create(Review review) {
        return this.reviewRepository.saveAndFlush(review);
    }

    @Override
    public Review createPropertyReview(Review review, Long propertyId) {
        Property property = this.propertyRepository.findById(propertyId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!this.guestEligibleToReviewProperty(review.getAuthor().getId(), propertyId)) {
            throw new RuntimeException("Review author not eligible to review property");
        }
        review.setProperty(property);
        return reviewRepository.saveAndFlush(review);
    }

    @Override
    public Review createHostReview(Review review, UUID hostId) {
        Account host = this.accountService.find(hostId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Host not found"));
        if (!this.guestEligibleToReviewHost(review.getAuthor().getId(), hostId)) {
            throw new RuntimeException("Review author not eligible to review host");
        }
        review.setHost(host);
        return reviewRepository.saveAndFlush(review);
    }

    @Override
    public Review update(Review review) {
        return reviewRepository.saveAndFlush(review);
    }

    @Override
    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }

    public boolean isBy(Account guest, Long id) {
        Optional<Review> review = this.reviewRepository.findById(id);
        return review.isPresent() && review.get().getAuthor().getId().equals(guest.getId());
    }

    @Override
    public boolean guestEligibleToReviewProperty(UUID guestId, Long propertyId) {
        Collection<Reservation> eligibleReservations = reservationRepository.findAllPastReservationsAtPropertyForGuestAfterDate(guestId, propertyId, LocalDate.now(clock).minusDays(reviewDeadline));
        Optional<Account> guest = accountService.find(guestId);
        if (guest.isEmpty() || guest.get().getRole() != AccountRole.GUEST || propertyRepository.findPropertyReviewByGuest(propertyId, guestId).isPresent()) {
            return false;
        }
        return !eligibleReservations.isEmpty();
    }

    @Override
    public boolean guestEligibleToReviewHost(UUID guestId, UUID hostId) {
        Collection<Reservation> eligibleReservations = reservationRepository.findAllPastReservationsAtHostAfterDate(guestId, hostId, LocalDate.now(clock).minusDays(reviewDeadline));
        Optional<Account> guest = accountService.find(guestId);
        if (guest.isEmpty() || guest.get().getRole() != AccountRole.GUEST || reviewRepository.findHostReviewByGuest(hostId, guestId).isPresent()) {
            return false;
        }
        return !eligibleReservations.isEmpty();
    }

    public Collection<Review> getAllReviewsForHost(UUID hostId) {
        return reviewRepository.findApprovedReviewsForHost(hostId);
    }

    public Collection<Review> getALlReviewsForProperty(Long propertyId) {
        return reviewRepository.findApprovedReviewsForProperty(propertyId);
    }

    @Override
    public Collection<Review> findAllUnapproved() {
        return this.reviewRepository.findAllByApproved(false);
    }

    @Override
    public Review approveReview(Review review) {
        review.setApproved(true);
        Review approvedReview = this.update(review);
        this.notificationService.createNotification(review);
        return approvedReview;
    }
}

package com.lunark.lunark.reviews.service;

import com.lunark.lunark.reviews.model.Review;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IReviewService<Review> {
    Collection<Review> findAll();
    Optional<Review> find(Long id);
    Review create(Review review);
    Review createPropertyReview(Review review, Long propertyId);
    Review createHostReview(Review review, UUID hostId);
    Review update(Review review);
    void delete(Long id);
    boolean guestEligibleToReviewProperty(UUID guestId, Long propertyId);
    boolean guestEligibleToReviewHost(UUID guestId, UUID hostId);

    Collection<com.lunark.lunark.reviews.model.Review> findAllUnapproved();

    com.lunark.lunark.reviews.model.Review approveReview(com.lunark.lunark.reviews.model.Review review);
}

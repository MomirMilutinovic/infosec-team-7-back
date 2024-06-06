package com.lunark.lunark.properties.repostiory;

import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface IPropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {
    Optional<Property> findById(Long id);
    @Query("select r from Property p join p.reviews r where p.id = ?1 and r.author.id = ?2")
    Optional<Review> findPropertyReviewByGuest(Long propertyId, UUID guestId);
}

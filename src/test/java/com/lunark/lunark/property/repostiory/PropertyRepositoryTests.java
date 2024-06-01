package com.lunark.lunark.property.repostiory;

import com.lunark.lunark.properties.repostiory.IPropertyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
public class PropertyRepositoryTests {
    @Autowired
    IPropertyRepository propertyRepository;

    @Test
    public void nonExistentPropertyAndNonExistentGuestShouldReturnNothing() {
        Assertions.assertTrue(propertyRepository.findPropertyReviewByGuest(1234L, UUID.randomUUID()).isEmpty());
    }

    @Test
    public void existentPropertyButGuestDidNotWriteReviewShouldReturnNothing() {
        Assertions.assertTrue(propertyRepository.findPropertyReviewByGuest(4L, UUID.randomUUID()).isEmpty());
    }

    @Test
    public void guestWroteReviewForPropertyShouldReturnReview() {
        Assertions.assertTrue(propertyRepository.findPropertyReviewByGuest(4L, UUID.randomUUID()).isPresent());
    }
}

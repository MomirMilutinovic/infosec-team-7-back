package com.lunark.lunark.reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyReviewEligibilityDto {
    private boolean eligible;
    private UUID guestId;
    private Long propertyId;
}

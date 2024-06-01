package com.lunark.lunark.reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostReviewEligibilityDto {
    private boolean eligible;
    private UUID guestId;
    private UUID hostId;
}

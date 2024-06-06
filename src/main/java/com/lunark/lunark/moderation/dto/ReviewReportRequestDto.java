package com.lunark.lunark.moderation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewReportRequestDto(
        LocalDateTime date,
        UUID reporterId,
        Long reviewId
) {
}

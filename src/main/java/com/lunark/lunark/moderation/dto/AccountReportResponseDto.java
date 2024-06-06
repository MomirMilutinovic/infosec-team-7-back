package com.lunark.lunark.moderation.dto;

import com.lunark.lunark.auth.model.Account;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountReportResponseDto (
    Long id,
    LocalDateTime date,
    UUID reporterId,
    UUID reportedId,
    String reason
) {
}

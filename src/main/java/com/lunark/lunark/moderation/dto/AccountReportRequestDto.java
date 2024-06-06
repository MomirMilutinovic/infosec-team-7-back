package com.lunark.lunark.moderation.dto;

import com.lunark.lunark.validation.AccountExistsConstraint;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountReportRequestDto(
        @NotNull
        @PastOrPresent
        LocalDateTime date,
        @NotNull
        @AccountExistsConstraint
        UUID reportedId,
        @NotNull
        @NotEmpty
        @Pattern(message="Report reason can contain alphanumeric characters and punctuation marks only", regexp = "[a-zA-Z0-9 \\.!\\?\'\"]+")
        String reason
) {
}

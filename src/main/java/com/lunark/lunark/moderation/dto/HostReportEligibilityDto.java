package com.lunark.lunark.moderation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HostReportEligibilityDto {
    UUID hostId;
    boolean eligible;
}

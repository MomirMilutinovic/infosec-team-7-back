package com.lunark.lunark.reservations.dto;

import com.lunark.lunark.reservations.model.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ReservationSearchDto {
    String propertyName;
    LocalDate startDate;
    LocalDate endDate;
    ReservationStatus status;
    UUID accountId;
}

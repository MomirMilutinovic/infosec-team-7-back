package com.lunark.lunark.properties.dto;

import com.lunark.lunark.properties.model.Address;
import com.lunark.lunark.properties.model.PricingMode;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.validation.AmenitiesExistConstraint;
import com.lunark.lunark.validation.HostExistsConstraint;
import com.lunark.lunark.validation.ValidMinMaxGuests;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@ValidMinMaxGuests
public class PropertyRequestDto {
    @PositiveOrZero
    Long id;
    @NotNull
    @NotEmpty
    @Pattern(message="The name of a property can contain only alphanumeric characters", regexp = "[a-zA-Z0-9 ]+")
    String name;
    @NotNull
    @Positive(message = "The minimum number of guests must be positive")
    int minGuests;
    @NotNull
    @Positive(message = "The maximum number of guests must be positive")
    int maxGuests;
    @NotNull
    @NotEmpty
    @Pattern(message="The description of a property can contain only alphanumeric characters and punctuation marks", regexp = "[a-zA-Z0-9 ,\\.!\\?\'\"]+")
    String description;
    @NotNull
    @Valid
    Address address;
    @NotNull
    PricingMode pricingMode;
    @PositiveOrZero
    int cancellationDeadline;
    boolean autoApproveEnabled;
    @AmenitiesExistConstraint
    List<@PositiveOrZero Long> amenityIds;
    @NotEmpty
    @Valid
    Collection<AvailabilityEntryDto> availabilityEntries;
    @NotNull
    Property.PropertyType type;
    @NotNull
    UUID hostId;
    @Min(-90)
    @Max(90)
    double latitude;
    @Min(-180)
    @Max(180)
    double longitude;
}

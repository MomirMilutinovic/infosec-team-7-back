package com.lunark.lunark.mapper;

import com.lunark.lunark.amenities.dto.AmenityResponseDto;
import com.lunark.lunark.amenities.model.Amenity;
import com.lunark.lunark.auth.repository.IAccountRepository;
import com.lunark.lunark.properties.dto.AvailabilityEntryDto;
import com.lunark.lunark.properties.dto.PropertyRequestDto;
import com.lunark.lunark.properties.dto.PropertyResponseDto;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.properties.model.PropertyAvailabilityEntry;
import com.lunark.lunark.reviews.dto.ReviewDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PropertyDtoMapper {
    private static ModelMapper modelMapper;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    public PropertyDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Property fromDtoToProperty(PropertyRequestDto propertyRequestDto) {
        Property property = modelMapper.map(propertyRequestDto, Property.class);

        List<PropertyAvailabilityEntry> availabilityEntries = propertyRequestDto.getAvailabilityEntries().stream()
                .map(availabilityEntryDto -> modelMapper.map(availabilityEntryDto, PropertyAvailabilityEntry.class))
                .collect(Collectors.toList());
        // We need to circumvent availability entry setter checks here
        // so old unchanged entries can be converted properly
        property.getAvailabilityEntries().clear();
        property.getAvailabilityEntries().addAll(availabilityEntries);

        List<Amenity> amenities = propertyRequestDto.getAmenityIds().stream()
                .map(id -> {
                    Amenity amenity = new Amenity();
                    amenity.setId(id);
                    return amenity;
                })
                .toList();
        property.setAmenities(amenities);
        property.setHost(accountRepository.getReferenceById(propertyRequestDto.getHostId()));

        return property;
    }

    public Property fromDtoToProperty(PropertyRequestDto propertyRequestDto, UUID hostId) {
        propertyRequestDto.setHostId(hostId);
        return this.fromDtoToProperty(propertyRequestDto);
    }

    public static PropertyResponseDto fromPropertyToDto(Property property) {
        PropertyResponseDto propertyResponseDto = modelMapper.map(property, PropertyResponseDto.class);
        List<AvailabilityEntryDto> availabilityEntryDtos = property.getAvailabilityEntries().stream()
                .map(propertyAvailabilityEntry -> modelMapper.map(propertyAvailabilityEntry, AvailabilityEntryDto.class))
                .collect(Collectors.toList());
        propertyResponseDto.setAvailabilityEntries(availabilityEntryDtos);

        List<AmenityResponseDto> amenities = property.getAmenities().stream()
                .map(amenity -> modelMapper.map(amenity, AmenityResponseDto.class))
                .toList();
        propertyResponseDto.setAmenities(amenities);

        List<ReviewDto> reviews = property.getReviews().stream()
                .map(review -> ReviewDtoMapper.toDto(review))
                .toList();
        propertyResponseDto.setReviews(reviews);
        propertyResponseDto.setHost(AccountDtoMapper.fromAccountToDTO(property.getHost()));

        return propertyResponseDto;
    }

    public static PropertyRequestDto fromPropertyToRequestDto(Property property) {
        PropertyRequestDto propertyRequestDto = modelMapper.map(property, PropertyRequestDto.class);
        List<AvailabilityEntryDto> availabilityEntryDtos = property.getAvailabilityEntries().stream()
                .map(propertyAvailabilityEntry -> modelMapper.map(propertyAvailabilityEntry, AvailabilityEntryDto.class))
                .collect(Collectors.toList());
        propertyRequestDto.setAvailabilityEntries(availabilityEntryDtos);

        List<Long> amenities = property.getAmenities().stream()
                .map(Amenity::getId)
                .toList();
        propertyRequestDto.setAmenityIds(amenities);

        return propertyRequestDto;
    }
}

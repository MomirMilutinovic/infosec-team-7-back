package com.lunark.lunark.properties.service;

import com.lunark.lunark.properties.dto.PropertySearchDto;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.properties.model.PropertyImage;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import com.lunark.lunark.properties.model.PropertyAvailabilityEntry;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPropertyService {
    Collection<Property> findAll();
    Optional<Property> find(Long id);
    Property create(Property property);
    Property update(Property property, Long id);

    List<Property> findUnapproved();

    void delete(Long id);
    Double getAverageGrade(Long id);
    void saveImage(Property property, MultipartFile file) throws IOException;
    Optional<PropertyImage> getImage(Long imageId);
    boolean changePricesAndAvailability(Long id, Collection<PropertyAvailabilityEntry> newPricesAndAvailability);
    List<Property> findByFilter(PropertySearchDto filter);

    List<Property> findAllPropertiesForHost(UUID hostId);
    Property deleteImages(Long id);
}

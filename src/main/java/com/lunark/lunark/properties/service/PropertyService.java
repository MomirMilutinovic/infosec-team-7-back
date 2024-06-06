package com.lunark.lunark.properties.service;


import com.lunark.lunark.properties.dto.PropertySearchDto;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.properties.model.PropertyAvailabilityEntry;
import com.lunark.lunark.properties.model.PropertyImage;
import com.lunark.lunark.properties.repostiory.IPropertyImageRepository;
import com.lunark.lunark.properties.repostiory.IPropertyRepository;
import com.lunark.lunark.properties.specification.PropertySpecification;
import com.lunark.lunark.reservations.service.IReservationService;
import com.lunark.lunark.reviews.model.Review;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.filter;

@Service
public class PropertyService implements IPropertyService {
    private IPropertyRepository propertyRepository;
    private final IPropertyImageRepository propertyImageRepository;
    private Clock clock;
    @Autowired
    private IReservationService reservationService;

    @Autowired
    public PropertyService(IPropertyRepository propertyRepository, IPropertyImageRepository propertyImageRepository, Clock clock, IReservationService reservationService) {
        this.propertyRepository = propertyRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.clock = clock;
        this.reservationService = reservationService;
    }

    @Override
    public Collection<Property> findAll() {
        return propertyRepository.findAll();
    }

    @Override
    public Optional<Property> find(Long id) {
        return propertyRepository.findById(id);
    }

    @Override
    public Property create(Property property) {
        try {
            for(PropertyAvailabilityEntry entry: property.getAvailabilityEntries()) {
                entry.setProperty(property);
            }
            propertyRepository.save(property);
            propertyRepository.flush();
            return property;
        } catch (ConstraintViolationException ex) {
            Set<ConstraintViolation<?>> errors = ex.getConstraintViolations();
            StringBuilder sb = new StringBuilder(1000);
            for (ConstraintViolation<?> error : errors) {
                sb.append(error.getMessage() + "\n");
            }
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                    sb.toString());
        }
    }

    @Override
    public Property update(Property newProperty, Long id) {
        try {
            Optional<Property> property = find(id);
            if (property.isEmpty()) {
                return this.create(newProperty);
            } else {
                boolean shouldRemoveApproval = this.shouldRemoveApproval(property.get(), newProperty);
                property.get().setClock(clock);
                Set<LocalDate> closedDates = getClosedDates(property.get(), newProperty);
                property.get().copyFields(newProperty);
                rejectPendingReservationsOnDates(closedDates, property.get());
                if (shouldRemoveApproval) {
                    property.get().setApproved(false);
                }
                propertyRepository.save(property.get());
                propertyRepository.flush();
                return property.get();
            }
        } catch (RuntimeException ex) {
            Throwable e = ex; Throwable c = null;
            while((e != null) && !((c = e.getCause()) instanceof ConstraintViolationException)){
                e = (RuntimeException) c;
            }
            if ((c != null) && (c instanceof ConstraintViolationException)) {
                ConstraintViolationException c2 = (ConstraintViolationException) c;
                Set<ConstraintViolation<?>> errors = c2.getConstraintViolations();
                StringBuilder sb = new StringBuilder(1000);
                for (ConstraintViolation<?> error : errors) {
                    sb.append(error.getMessage() + "\n");
                }
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, sb.toString());
            }
            throw ex;
        }
    }

    public Property deleteImages(Long id) {
        Optional<Property> property = find(id);
        property.get().getImages().clear();
        propertyRepository.save(property.get());
        propertyRepository.flush();
        return property.get();
    }

    @Override
    public boolean changePricesAndAvailability(Long id, Collection<PropertyAvailabilityEntry> newPricesAndAvailability) {
        Optional<Property> propertyOptional = this.find(id);
        if (propertyOptional.isEmpty()){
            return false;
        }
        Property property = propertyOptional.get();
        property.setClock(clock);
        for(PropertyAvailabilityEntry entry: newPricesAndAvailability) {
            entry.setProperty(property);
        }
        if (!property.setAvailabilityEntries(newPricesAndAvailability)) {
            return false;
        }
        update(property, property.getId());
        return true;

    }

    @Override
    public List<Property> findByFilter(PropertySearchDto filter) {
        Specification<Property> specification = new PropertySpecification(filter);
        return propertyRepository.findAll(specification);
    }

    @Override
    public List<Property> findAllPropertiesForHost(UUID hostId) {
        return propertyRepository.findAll().stream().filter(property -> Objects.equals(property.getHost().getId(), hostId)).toList();
    }


    @Override
    public List<Property> findUnapproved() {
        return propertyRepository.findAll().stream().filter(property -> !property.isApproved()).toList();
    }

    @Override
    public void delete(Long id) {
        propertyRepository.deleteById(id);
        propertyRepository.flush();
    }

    @Override
    public Double getAverageGrade(Long id) {
        Optional<Property> property = this.find(id);
        if (property.isEmpty()){
            return null;
        }
        Property foundProperty = property.get();
        // TODO: Convert average calculation to JPQL
        return calculateAverageGrade(foundProperty.getReviews());
    }

    private Double calculateAverageGrade(Collection<Review> reviews) {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToDouble(Review::getRating).sum();
        return sum / reviews.size();
    }

    @Override
    public void saveImage(Property property, MultipartFile file) throws IOException {
        byte[] byteObjects = new byte[file.getBytes().length];

        int i = 0;

        for (byte b : file.getBytes()){
            byteObjects[i++] = b;
        }

        PropertyImage propertyImage = new PropertyImage();
        propertyImage.setImageData(byteObjects);
        propertyImage.setMimeType(file.getContentType());

        property.getImages().add(propertyImage);

        propertyRepository.save(property);
        propertyImageRepository.save(propertyImage);
    }

    @Override
    @Transactional
    public Optional<PropertyImage> getImage(Long imageId) {
        return propertyImageRepository.findById(imageId);
    }

    private Set<LocalDate> getClosedDates(Property property, Property newProperty) {
        Set<LocalDate> closedDates = property.getAvailabilityEntries().stream().map(propertyAvailabilityEntry -> propertyAvailabilityEntry.getDate()).collect(Collectors.toSet());
        Set<LocalDate> newDates = newProperty.getAvailabilityEntries().stream().map(propertyAvailabilityEntry -> propertyAvailabilityEntry.getDate()).collect(Collectors.toSet());
        closedDates.removeAll(newDates);
        return closedDates;
    }

    private void rejectPendingReservationsOnDates(Set<LocalDate> dates, Property property) {
        for (LocalDate date: dates) {
            reservationService.rejectAllPendingReservationsAtPropertyThatContainDate(property.getId(), date);
        }
    }

    private boolean shouldRemoveApproval(Property oldProperty, Property changedProperty) {
        return !oldProperty.getName().equals(changedProperty.getName()) ||
                oldProperty.getLatitude() != changedProperty.getLatitude() ||
                oldProperty.getLongitude() != changedProperty.getLongitude() ||
                !(oldProperty.getAmenities().containsAll(changedProperty.getAmenities()) && changedProperty.getAmenities().containsAll(oldProperty.getAmenities())) ||
                oldProperty.getMinGuests() != changedProperty.getMinGuests() ||
                oldProperty.getMaxGuests() != changedProperty.getMaxGuests() ||
                !oldProperty.getDescription().equals(changedProperty.getDescription()) ||
                !oldProperty.getType().equals(changedProperty.getType()) ||
                !oldProperty.getAddress().equals(changedProperty.getAddress());
    }
}

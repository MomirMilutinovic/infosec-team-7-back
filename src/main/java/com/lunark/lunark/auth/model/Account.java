package com.lunark.lunark.auth.model;

import com.lunark.lunark.notifications.model.GuestNotificationSettings;
import com.lunark.lunark.notifications.model.HostNotificationSettings;
import com.lunark.lunark.notifications.model.NotificationType;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reviews.model.Review;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.Data;
import jakarta.persistence.CascadeType;
import lombok.Getter;
import org.hibernate.annotations.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;

@Entity
@SQLDelete(sql
        = "UPDATE account "
        + "SET deleted = true "
        + "WHERE id = ?")
@Where(clause = "deleted = false")
@Data
public class Account implements Serializable {
    @Id
    private UUID id;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column
    private String name;
    @Column
    private String surname;
    @Column
    private String address;
    @Column
    private String phoneNumber;
    @Column
    private AccountRole role;
    @Embedded
    private ProfileImage profileImage;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="account_reviews",
            joinColumns = {@JoinColumn(name = "account_id")},
            inverseJoinColumns = {@JoinColumn(name = "reviews_id")}
    )
    private Collection<Review> reviews;
    @ManyToMany
    private Set<Property> favoriteProperties;
    @OneToMany(mappedBy = "guest")
    private Set<Reservation> reservations;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private boolean deleted = false;

    private GuestNotificationSettings guestNotificationSettings;
    private HostNotificationSettings hostNotificationSettings;

    @Formula("(select count(*) from reservation r where r.status = 3 and r.guest_id = id)")
    private int cancelCount;

    public int getCancelCount() {
        return cancelCount;
    }

    public void setCancelCount(int cancelCount) {
        this.cancelCount = cancelCount;
    }

    @Formula("(select avg(r.rating) from review r join account_reviews ar on ar.reviews_id = r.id where ar.account_id = id and r.approved = true)")
    private Double averageRating;

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Account() {

    }

    public Account(UUID id, String email, String name, String surname, String address, String phoneNumber, AccountRole role, Collection<Review> reviews, HashSet<Property> favoriteProperties) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.reviews = reviews;
        this.favoriteProperties = favoriteProperties;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AccountRole getRole() {
        return role;
    }

    public void setRole(AccountRole role) {
        this.role = role;
    }

    public Collection<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Collection<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Property> getFavoriteProperties() {
        return favoriteProperties;
    }

    public void setFavoriteProperties(Set<Property> favoriteProperties) {
        this.favoriteProperties = favoriteProperties;
    }

    public ProfileImage getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(ProfileImage profileImage) {
        this.profileImage = profileImage;
    }

    public void toggleNotifications(NotificationType type) {
        switch (type) {
            case HOST_REVIEW -> hostNotificationSettings.setNotifyOnHostReview(!hostNotificationSettings.isNotifyOnHostReview());
            case PROPERTY_REVIEW -> hostNotificationSettings.setNotifyOnPropertyReview(!hostNotificationSettings.isNotifyOnPropertyReview());
            case RESERVATION_ACCEPTED, RESERVATION_REJECTED -> guestNotificationSettings.setNotifyOnReservationRequestResponse(!guestNotificationSettings.isNotifyOnReservationRequestResponse());
            case RESERVATION_CREATED -> hostNotificationSettings.setNotifyOnReservationCreation(!hostNotificationSettings.isNotifyOnReservationCreation());
            case RESERVATION_CANCELED -> hostNotificationSettings.setNotifyOnReservationCancellation(!hostNotificationSettings.isNotifyOnReservationCancellation());
        }
    }
}

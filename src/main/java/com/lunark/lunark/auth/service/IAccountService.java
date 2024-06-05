package com.lunark.lunark.auth.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.notifications.model.NotificationType;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.reservations.model.Reservation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountService {
    Collection<Account> findAll();
    Account create(Account account);
    Optional<Account> find(UUID id);
    Optional<Account> find(String email);
    Account update(Account account);
    boolean delete(UUID id);
    boolean updatePassword(UUID id, String oldPassword, String newPassword);
    void addToFavorites(UUID id, Property property);
    Double getAverageGrade(UUID id);
    Collection<Property> getFavoriteProperties(UUID accountId);
    void removeFromFavorites(UUID id, Property property);
    void saveProfileImage(UUID accountId, MultipartFile file) throws IOException;
    void saveAndFlush(Account account);
    public void cancelAllReservations(List<Reservation> reservationList);
    Account toggleNotifications(UUID accountId, NotificationType type);
}

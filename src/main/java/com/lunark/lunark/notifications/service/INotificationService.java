package com.lunark.lunark.notifications.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.notifications.model.Notification;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reviews.model.Review;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface INotificationService {
    Collection<Notification> getAllNotifications(UUID accountId);
    Notification create(Notification notification);

    Notification createNotification(Review review);
    Notification createNotification(Reservation reservation);

    long getUnreadNotificationCount(String email);
    void subscribe(ISubscriber subscriber);

    void markAsRead(Long id);

    Optional<Notification> findById(Long id);
}

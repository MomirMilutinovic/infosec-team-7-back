package com.lunark.lunark.notifications.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.notifications.model.Notification;
import com.lunark.lunark.notifications.model.NotificationType;
import com.lunark.lunark.notifications.repository.INotificationRepository;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reservations.model.ReservationStatus;
import com.lunark.lunark.reviews.model.Review;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class NotificationService implements INotificationService {
    private INotificationRepository notificationRepository;
    private Clock clock;
    private List<ISubscriber> subscribers;

    @Autowired
    public NotificationService(INotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
        this.subscribers = new ArrayList<>();
    }

    @Override
    @Transactional
    public Collection<Notification> getAllNotifications(UUID accountId) {
        Collection<Notification> notifications = this.notificationRepository.findByAccount_IdOrderByDateDesc(accountId);
        this.notificationRepository.markAllNotificationsAsRead(accountId);
        return notifications;
    }

    @Override
    public Notification create(Notification notification) {
        if (!this.shouldSendNotification(notification)) {
            return null;
        }
        Notification newNotification = this.notificationRepository.saveAndFlush(notification);
        this.subscribers.forEach(subscriber -> subscriber.notify(newNotification));
        return newNotification;
    }

    private boolean shouldSendNotification(Notification notification) {
        return switch (notification.getType()) {
            case PROPERTY_REVIEW -> notification.getAccount().getHostNotificationSettings().isNotifyOnPropertyReview();
            case HOST_REVIEW -> notification.getAccount().getHostNotificationSettings().isNotifyOnHostReview();
            case RESERVATION_CREATED -> notification.getAccount().getHostNotificationSettings().isNotifyOnReservationCreation();
            case RESERVATION_CANCELED -> notification.getAccount().getHostNotificationSettings().isNotifyOnReservationCancellation();
            case RESERVATION_ACCEPTED, RESERVATION_REJECTED -> notification.getAccount().getGuestNotificationSettings().isNotifyOnReservationRequestResponse();
        };
    }

    @Override
    public Notification createNotification(Review review) {
        String message = createReviewNotificationMessage(review);
        Notification notification = new Notification(
                message,
                ZonedDateTime.now(clock),
                review.getType().equals(Review.ReviewType.PROPERTY) ? NotificationType.PROPERTY_REVIEW : NotificationType.HOST_REVIEW,
                review.getType().equals(Review.ReviewType.PROPERTY) ? review.getProperty().getHost() : review.getHost());
        return this.create(notification);
    }


    private String createReviewNotificationMessage(Review review) {
        if (review.getType().equals(Review.ReviewType.HOST)) {
            return "New host review";
        }
        return "New property review for " + review.getProperty().getName();
    }

    @Override
    public Notification createNotification(Reservation reservation) {
        String message = createReservationNotificationMessage(reservation);
        Account account = determineAccountForNotification(reservation);
        NotificationType notificationType = determineNotificationType(reservation);

        Notification notification = new Notification(
                message,
                ZonedDateTime.now(clock),
                notificationType,
                account
        );

        return this.create(notification);
    }

    private Account determineAccountForNotification(Reservation reservation) {
        if (reservation.getStatus().equals(ReservationStatus.ACCEPTED) ||
                reservation.getStatus().equals(ReservationStatus.REJECTED)) {
            return reservation.getGuest();
        } else if (reservation.getStatus().equals(ReservationStatus.CANCELLED) ||
                reservation.getStatus().equals(ReservationStatus.PENDING)) {
            return reservation.getProperty().getHost();
        }
        throw new IllegalArgumentException("Invalid reservation status");
    }

    private NotificationType determineNotificationType(Reservation reservation) {
        switch (reservation.getStatus()) {
            case ACCEPTED:
                return NotificationType.RESERVATION_ACCEPTED;
            case REJECTED:
                return NotificationType.RESERVATION_REJECTED;
            case CANCELLED:
                return NotificationType.RESERVATION_CANCELED;
            case PENDING:
                return NotificationType.RESERVATION_CREATED;
            default:
                throw new IllegalArgumentException("Invalid reservation status");
        }
    }

    private String createReservationNotificationMessage(Reservation reservation) {
        switch (reservation.getStatus()) {
            case ACCEPTED:
                return "Reservation at " + reservation.getProperty().getName() + " has been accepted.";
            case REJECTED:
                return "Reservation at " + reservation.getProperty().getName() + " has been rejected.";
            case CANCELLED:
                return "Reservation at " + reservation.getProperty().getName() + " has been cancelled by " + reservation.getGuest().getName() + " " + reservation.getGuest().getSurname();
            case PENDING:
                return "Reservation at " + reservation.getProperty().getName() + " has been created by " + reservation.getGuest().getName() + " " + reservation.getGuest().getSurname();
            default:
                throw new IllegalArgumentException("Invalid reservation status");
        }
    }


    @Override
    public long getUnreadNotificationCount(String email) {
        return this.notificationRepository.countByAccount_EmailAndRead(email, false);
    }

    @Override
    public void subscribe(ISubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        this.notificationRepository.markAllNotificationAsRead(id);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return this.notificationRepository.findById(id);
    }

}

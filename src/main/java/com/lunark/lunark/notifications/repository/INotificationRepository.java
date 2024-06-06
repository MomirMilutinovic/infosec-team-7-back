package com.lunark.lunark.notifications.repository;

import com.lunark.lunark.notifications.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {
    long countByAccount_EmailAndRead(String email, boolean read);
    Collection<Notification> findByAccount_IdOrderByDateDesc(UUID id);
    @Query("update Notification n set n.read=true where n.account.id = ?1")
    @Modifying
    void markAllNotificationsAsRead(UUID accountId);
    @Query("update Notification n set n.read=true where n.id = ?1")
    @Modifying
    void markAllNotificationAsRead(Long id);
}

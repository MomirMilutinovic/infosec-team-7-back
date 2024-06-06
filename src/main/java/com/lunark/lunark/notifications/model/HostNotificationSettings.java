package com.lunark.lunark.notifications.model;

import com.lunark.lunark.auth.model.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class HostNotificationSettings {
    @Column(columnDefinition = "boolean default true")
    private boolean notifyOnReservationCreation;
    @Column(columnDefinition = "boolean default true")
    private boolean notifyOnReservationCancellation;
    @Column(columnDefinition = "boolean default true")
    private boolean notifyOnHostReview;
    @Column(columnDefinition = "boolean default true")
    private boolean notifyOnPropertyReview;
}

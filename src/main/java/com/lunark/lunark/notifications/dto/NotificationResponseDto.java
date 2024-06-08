package com.lunark.lunark.notifications.dto;

import com.lunark.lunark.notifications.model.NotificationType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private String text;
    private ZonedDateTime date;
    private boolean read;
    private UUID accountId;
    private String type;
}

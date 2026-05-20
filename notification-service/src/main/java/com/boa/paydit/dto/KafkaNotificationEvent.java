package com.boa.paydit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaNotificationEvent {
    private Long recipientId;
    private String templateName;
    private String subject;
    private String message;
    private String channel;
    private String email;
    private String phone;
    private Long timestamp;

    public KafkaNotificationEvent(Long recipientId, String templateName, String subject, String message, String channel) {
        this.recipientId = recipientId;
        this.templateName = templateName;
        this.subject = subject;
        this.message = message;
        this.channel = channel;
        this.timestamp = System.currentTimeMillis();
    }
}

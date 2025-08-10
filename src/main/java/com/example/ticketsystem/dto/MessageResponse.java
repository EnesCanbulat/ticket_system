package com.example.ticketsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(
        Long id,
        String senderType,
        String senderName,
        String senderEmail,
        String message,
        LocalDateTime createdAt,
        MessageMetadata metadata
) {
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");


    public static MessageResponse basic(Long id, String senderType, String senderName,
                                        String message, LocalDateTime createdAt) {
        return new MessageResponse(id, senderType, senderName, null, message, createdAt, null);
    }


    public static MessageResponse detailed(Long id, String senderType, String senderName,
                                           String senderEmail, String message, LocalDateTime createdAt) {
        var metadata = new MessageMetadata(
                message != null ? message.length() : 0,
                createdAt != null ? createdAt.format(DISPLAY_FORMATTER) : null
        );

        return new MessageResponse(id, senderType, senderName, senderEmail,
                message, createdAt, metadata);
    }


    public boolean isFromCustomer() {
        return "CUSTOMER".equals(senderType);
    }

    public boolean isFromAgent() {
        return "AGENT".equals(senderType);
    }

    public boolean isFromSystem() {
        return "SYSTEM".equals(senderType);
    }

    public String getShortMessage(int maxLength) {
        if (message == null) return null;
        return message.length() <= maxLength ?
                message : message.substring(0, maxLength) + "...";
    }

    public String getFormattedDateTime() {
        return createdAt != null ? createdAt.format(DISPLAY_FORMATTER) : "Unknown";
    }


    public record MessageMetadata(
            int messageLength,
            String formattedDate
    ) {
        public boolean isLongMessage() {
            return messageLength > 1000;
        }

        public String getLengthCategory() {
            return switch (messageLength) {
                case 0 -> "Empty";
                case 1 -> "Very Short";
                default -> messageLength <= 100 ? "Short" :
                        messageLength <= 500 ? "Medium" :
                                messageLength <= 1000 ? "Long" : "Very Long";
            };
        }
    }
}
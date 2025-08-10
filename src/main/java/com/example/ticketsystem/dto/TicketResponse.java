package com.example.ticketsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TicketResponse(
        Long id,
        String title,
        String description,
        String status,
        String priority,
        String customerName,
        String customerEmail,
        String agentName,
        String agentEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt,
        List<MessageResponse> messages,
        TicketMetrics metrics
) {

    public static TicketResponse basic(Long id, String title, String description,
                                       String status, String priority, String customerName,
                                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new TicketResponse(id, title, description, status, priority,
                customerName, null, null, null,
                createdAt, updatedAt, null, null, null);
    }


    public static TicketResponse detailed(Long id, String title, String description,
                                          String status, String priority,
                                          String customerName, String customerEmail,
                                          String agentName, String agentEmail,
                                          LocalDateTime createdAt, LocalDateTime updatedAt,
                                          LocalDateTime closedAt,
                                          List<MessageResponse> messages) {
        var metrics = new TicketMetrics(
                Duration.between(createdAt, LocalDateTime.now()),
                closedAt != null ? Duration.between(createdAt, closedAt) : null,
                messages != null ? messages.size() : 0
        );

        return new TicketResponse(id, title, description, status, priority,
                customerName, customerEmail, agentName, agentEmail,
                createdAt, updatedAt, closedAt, messages, metrics);
    }


    public boolean isClosed() {
        return closedAt != null;
    }

    public boolean hasAgent() {
        return agentName != null && !agentName.trim().isEmpty();
    }

    public boolean hasMessages() {
        return messages != null && !messages.isEmpty();
    }

    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }


    public record TicketMetrics(
            Duration age,
            Duration resolutionTime,
            int messageCount
    ) {
        public String getAgeInHours() {
            return age != null ? "%.1f hours".formatted(age.toMinutes() / 60.0) : "Unknown";
        }

        public String getResolutionTimeInHours() {
            return resolutionTime != null ?
                    "%.1f hours".formatted(resolutionTime.toMinutes() / 60.0) : "Not resolved";
        }
    }
}
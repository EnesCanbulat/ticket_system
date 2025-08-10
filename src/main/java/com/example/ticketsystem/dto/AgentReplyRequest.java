package com.example.ticketsystem.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record AgentReplyRequest(
        Long agentId,

        @NotBlank(message = "Mesaj boş olamaz")
        @Size(min = 1, max = 5000, message = "Mesaj 1-5000 karakter arasında olmalıdır")
        String message,


        Long newStatusId,


        Boolean isInternal
) {

    public AgentReplyRequest(Long agentId, String message) {
        this(agentId, message, null, false);
    }


    public AgentReplyRequest(Long agentId, String message, Long newStatusId) {
        this(agentId, message, newStatusId, false);
    }


    public boolean hasValidData() {
        return message != null && !message.trim().isEmpty();
    }

    public String getTrimmedMessage() {
        return message != null ? message.trim() : null;
    }

    public boolean hasStatusUpdate() {
        return newStatusId != null && newStatusId > 0;
    }

    public boolean isInternalMessage() {
        return Boolean.TRUE.equals(isInternal);
    }

    public int getMessageLength() {
        return message != null ? message.trim().length() : 0;
    }
}
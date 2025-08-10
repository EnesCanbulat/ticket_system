package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public record SendMessageRequest(
        @NotNull(message = "Gönderen ID'si gerekli")
        @Min(value = 1, message = "Gönderen ID'si pozitif bir sayı olmalıdır")
        Long senderId,

        @NotBlank(message = "Mesaj boş olamaz")
        @Size(min = 1, max = 5000, message = "Mesaj 1-5000 karakter arasında olmalıdır")
        String message
) {

    public boolean hasValidData() {
        return senderId != null && senderId > 0 &&
                message != null && !message.trim().isEmpty();
    }

    public String getTrimmedMessage() {
        return message != null ? message.trim() : null;
    }

    public int getMessageLength() {
        return message != null ? message.trim().length() : 0;
    }

    public boolean isLongMessage() {
        return getMessageLength() > 1000;
    }
}
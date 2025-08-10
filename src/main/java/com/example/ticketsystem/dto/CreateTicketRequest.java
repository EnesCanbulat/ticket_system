package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;


public record CreateTicketRequest(
        @NotNull(message = "Müşteri ID'si gerekli")
        @Min(value = 1, message = "Müşteri ID'si pozitif bir sayı olmalıdır")
        Long customerId,

        @NotBlank(message = "Başlık boş olamaz")
        @Size(min = 5, max = 200, message = "Başlık 5-200 karakter arasında olmalıdır")
        String title,

        @NotBlank(message = "Açıklama boş olamaz")
        @Size(min = 10, max = 5000, message = "Açıklama 10-5000 karakter arasında olmalıdır")
        String description,

        @Min(value = 1, message = "Öncelik ID'si pozitif bir sayı olmalıdır")
        Long priorityId
) {

    public CreateTicketRequest {

        if (title != null) {
            title = title.trim();
        }
        if (description != null) {
            description = description.trim();
        }

    }


    public CreateTicketRequest(Long customerId, String title, String description) {
        this(customerId, title, description, null); // null göndererek service'in default atamasını sağlıyoruz
    }


    public Long getEffectivePriorityId() {
        return priorityId != null ? priorityId : 2L; // Default: Normal priority (ID=2)
    }


    public boolean hasValidData() {
        return customerId != null && customerId > 0 &&
                title != null && !title.trim().isEmpty() &&
                description != null && !description.trim().isEmpty();

    }

    public String getTrimmedTitle() {
        return title != null ? title.trim() : null;
    }

    public String getTrimmedDescription() {
        return description != null ? description.trim() : null;
    }


    public boolean hasPriorityId() {
        return priorityId != null && priorityId > 0;
    }
}
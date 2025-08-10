package com.example.ticketsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


public record AssignTicketRequest(
        @NotNull(message = "Temsilci ID'si gerekli")
        @Min(value = 1, message = "Temsilci ID'si pozitif bir sayı olmalıdır")
        Long agentId,

        String note
) {

    public AssignTicketRequest(Long agentId) {
        this(agentId, null);
    }


    public boolean hasValidAgentId() {
        return agentId != null && agentId > 0;
    }

    public String getTrimmedNote() {
        return note != null ? note.trim() : null;
    }

    public boolean hasNote() {
        return note != null && !note.trim().isEmpty();
    }
}

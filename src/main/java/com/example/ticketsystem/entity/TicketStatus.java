package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
@Table(name = "ticket_statuses", indexes = {
        @Index(name = "idx_status_name", columnList = "name")
})
public class TicketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Durum adı boş olamaz")
    @Size(min = 2, max = 50, message = "Durum adı 2-50 karakter arasında olmalıdır")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Size(max = 200, message = "Açıklama 200 karakterden fazla olamaz")
    @Column(length = 200)
    private String description;


    public static final String OPEN = "Açık";
    public static final String ASSIGNED = "Atanmış";
    public static final String IN_PROGRESS = "İşlemde";
    public static final String WAITING = "Beklemede";
    public static final String RESOLVED = "Çözüldü";
    public static final String CLOSED = "Kapalı";


    public TicketStatus() {}

    public TicketStatus(String name, String description) {
        this.name = name;
        this.description = description;
    }


    public boolean isOpen() {
        return OPEN.equals(name);
    }

    public boolean isAssigned() {
        return ASSIGNED.equals(name);
    }

    public boolean isInProgress() {
        return IN_PROGRESS.equals(name);
    }

    public boolean isWaiting() {
        return WAITING.equals(name);
    }

    public boolean isResolved() {
        return RESOLVED.equals(name);
    }

    public boolean isClosed() {
        return CLOSED.equals(name);
    }

    public boolean isActive() {
        return !isClosed() && !isResolved();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TicketStatus status &&
                Objects.equals(id, status.id) &&
                Objects.equals(name, status.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "TicketStatus{id=%d, name='%s', description='%s'}"
                .formatted(id, name, description);
    }
}
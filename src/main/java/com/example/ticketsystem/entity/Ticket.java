package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_customer", columnList = "customer_id"),
        @Index(name = "idx_ticket_agent", columnList = "agent_id"),
        @Index(name = "idx_ticket_status", columnList = "status_id"),
        @Index(name = "idx_ticket_priority", columnList = "priority_id"),
        @Index(name = "idx_ticket_created_at", columnList = "created_at"),
        @Index(name = "idx_ticket_updated_at", columnList = "updated_at"),
        @Index(name = "idx_ticket_customer_status", columnList = "customer_id, status_id"),
        @Index(name = "idx_ticket_agent_status", columnList = "agent_id, status_id"),
        @Index(name = "idx_ticket_status_priority", columnList = "status_id, priority_id")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Müşteri bilgisi gerekli")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @NotBlank(message = "Başlık boş olamaz")
    @Size(min = 5, max = 200, message = "Başlık 5-200 karakter arasında olmalıdır")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Açıklama boş olamaz")
    @Size(min = 10, max = 5000, message = "Açıklama 10-5000 karakter arasında olmalıdır")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    @NotNull(message = "Durum bilgisi gerekli")
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "priority_id", nullable = false)
    @NotNull(message = "Öncelik bilgisi gerekli")
    private TicketPriority priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;



    public Ticket() {}

    public Ticket(Customer customer, String title, String description,
                  TicketStatus status, TicketPriority priority) {
        this.customer = customer;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }


    public Ticket(Customer customer, Agent agent, String title, String description,
                  TicketStatus status, TicketPriority priority) {
        this.customer = customer;
        this.agent = agent;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    @PrePersist
    protected void onCreate() {
        var now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public boolean isOpen() {
        return status != null && "Açık".equals(status.getName());
    }

    public boolean isAssigned() {
        return agent != null && status != null && "Atanmış".equals(status.getName());
    }

    public boolean isClosed() {
        return status != null && "Kapalı".equals(status.getName());
    }

    public boolean isResolved() {
        return status != null && "Çözüldü".equals(status.getName());
    }

    public boolean isActive() {
        return status != null && !isClosed() && !isResolved();
    }

    public boolean isOverdue() {
        if (closedAt != null) return false; // Already closed

        var now = LocalDateTime.now();
        var hoursOpen = Duration.between(createdAt, now).toHours();


        if (priority != null) {
            return switch (priority.getLevel()) {
                case 4, 5 -> hoursOpen > 4;   // Critical/Urgent: 4 hours
                case 3 -> hoursOpen > 24;     // High: 24 hours
                case 2 -> hoursOpen > 72;     // Normal: 72 hours
                case 1 -> hoursOpen > 168;    // Low: 1 week
                default -> hoursOpen > 72;
            };
        }

        return hoursOpen > 72; // Default 72 hours
    }

    public void assignToAgent(Agent agent) {
        this.agent = agent;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(TicketStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();


        if (newStatus != null && ("Kapalı".equals(newStatus.getName()) ||
                "Çözüldü".equals(newStatus.getName()))) {
            if (closedAt == null) {
                closedAt = LocalDateTime.now();
            }
        }
    }

    public void close() {
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Duration getAge() {
        return Duration.between(createdAt, LocalDateTime.now());
    }

    public Duration getResolutionTime() {
        if (closedAt == null) {
            return null;
        }
        return Duration.between(createdAt, closedAt);
    }

    public String getAgeFormatted() {
        var age = getAge();
        var hours = age.toHours();
        var days = age.toDays();

        if (days > 0) {
            return days + " gün " + (hours % 24) + " saat";
        } else if (hours > 0) {
            return hours + " saat " + (age.toMinutes() % 60) + " dakika";
        } else {
            return age.toMinutes() + " dakika";
        }
    }

    public String getResolutionTimeFormatted() {
        var resolutionTime = getResolutionTime();
        if (resolutionTime == null) {
            return "Henüz çözülmedi";
        }

        var hours = resolutionTime.toHours();
        var days = resolutionTime.toDays();

        if (days > 0) {
            return days + " gün " + (hours % 24) + " saat";
        } else if (hours > 0) {
            return hours + " saat " + (resolutionTime.toMinutes() % 60) + " dakika";
        } else {
            return resolutionTime.toMinutes() + " dakika";
        }
    }

    public TicketUrgency getUrgency() {
        var age = getAge();
        var priorityLevel = priority != null ? priority.getLevel() : 2;


        if (priorityLevel >= 4 && age.toHours() > 2) {
            return TicketUrgency.CRITICAL;
        }


        if (priorityLevel >= 3 || isOverdue()) {
            return TicketUrgency.HIGH;
        }


        if (priorityLevel == 2) {
            return TicketUrgency.MEDIUM;
        }

        return TicketUrgency.LOW;
    }

    public enum TicketUrgency {
        CRITICAL("Kritik", "🚨", "#dc3545"),
        HIGH("Yüksek", "⚠️", "#fd7e14"),
        MEDIUM("Orta", "📋", "#ffc107"),
        LOW("Düşük", "📝", "#28a745");

        private final String displayName;
        private final String emoji;
        private final String colorCode;

        TicketUrgency(String displayName, String emoji, String colorCode) {
            this.displayName = displayName;
            this.emoji = emoji;
            this.colorCode = colorCode;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
        public String getColorCode() { return colorCode; }

        public String getFormattedName() {
            return emoji + " " + displayName;
        }

        @Override
        public String toString() {
            return getFormattedName();
        }
    }

    public String getDisplayTitle() {
        if (title == null) return "Başlıksız Ticket";
        return title.length() > 50 ? title.substring(0, 50) + "..." : title;
    }

    public String getShortDescription(int maxLength) {
        if (description == null) return "";
        return description.length() <= maxLength ?
                description : description.substring(0, maxLength) + "...";
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Bilinmeyen";
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt == null) return "Bilinmeyen";
        return updatedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public String getStatusWithIcon() {
        if (status == null) return "❓ Bilinmeyen";

        return switch (status.getName()) {
            case "Açık" -> "🆕 " + status.getName();
            case "Atanmış" -> "👤 " + status.getName();
            case "İşlemde" -> "⚙️ " + status.getName();
            case "Beklemede" -> "⏳ " + status.getName();
            case "Çözüldü" -> "✅ " + status.getName();
            case "Kapalı" -> "🔒 " + status.getName();
            default -> "❓ " + status.getName();
        };
    }

    public String getPriorityWithIcon() {
        if (priority == null) return "❓ Bilinmeyen";

        return switch (priority.getLevel()) {
            case 1 -> "🟢 " + priority.getName();
            case 2 -> "🔵 " + priority.getName();
            case 3 -> "🟡 " + priority.getName();
            case 4 -> "🟠 " + priority.getName();
            case 5 -> "🔴 " + priority.getName();
            default -> "❓ " + priority.getName();
        };
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Ticket ticket &&
                Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ticket{id=%d, title='%s', status='%s', priority='%s', createdAt=%s}"
                .formatted(id, getDisplayTitle(),
                        status != null ? status.getName() : "null",
                        priority != null ? priority.getName() : "null",
                        createdAt);
    }


    public String toJson() {
        return """
            {
                "id": %d,
                "title": "%s",
                "description": "%s",
                "status": "%s",
                "priority": "%s",
                "urgency": "%s",
                "customerName": "%s",
                "agentName": "%s",
                "isOverdue": %s,
                "age": "%s",
                "resolutionTime": "%s",
                "createdAt": "%s",
                "updatedAt": "%s",
                "closedAt": "%s"
            }
            """.formatted(
                id,
                title,
                getShortDescription(100),
                status != null ? status.getName() : "null",
                priority != null ? priority.getName() : "null",
                getUrgency(),
                customer != null ? customer.getName() : "null",
                agent != null ? agent.getName() : "null",
                isOverdue(),
                getAgeFormatted(),
                getResolutionTimeFormatted(),
                getFormattedCreatedAt(),
                getFormattedUpdatedAt(),
                closedAt != null ? closedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "null"
        );
    }


    public static TicketBuilder builder() {
        return new TicketBuilder();
    }

    public static class TicketBuilder {
        private Customer customer;
        private Agent agent;
        private String title;
        private String description;
        private TicketStatus status;
        private TicketPriority priority;
        private LocalDateTime createdAt;

        public TicketBuilder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public TicketBuilder agent(Agent agent) {
            this.agent = agent;
            return this;
        }

        public TicketBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TicketBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TicketBuilder status(TicketStatus status) {
            this.status = status;
            return this;
        }

        public TicketBuilder priority(TicketPriority priority) {
            this.priority = priority;
            return this;
        }

        public TicketBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TicketBuilder now() {
            this.createdAt = LocalDateTime.now();
            return this;
        }

        public Ticket build() {
            var ticket = new Ticket(customer, agent, title, description, status, priority);
            if (createdAt != null) {
                ticket.setCreatedAt(createdAt);
            }
            return ticket;
        }
    }


    public static Ticket createNewTicket(Customer customer, String title, String description,
                                         TicketStatus openStatus, TicketPriority priority) {
        return new Ticket(customer, title, description, openStatus, priority);
    }

    public static Ticket createAssignedTicket(Customer customer, Agent agent, String title,
                                              String description, TicketStatus assignedStatus,
                                              TicketPriority priority) {
        return new Ticket(customer, agent, title, description, assignedStatus, priority);
    }


    public boolean canBeAssigned() {
        return isOpen() && agent == null;
    }

    public boolean canBeReassigned() {
        return isActive() && agent != null;
    }

    public boolean canBeClosed() {
        return isActive() && agent != null;
    }

    public boolean requiresAttention() {
        return isOverdue() || getUrgency() == TicketUrgency.CRITICAL;
    }

    public String getSummary() {
        return """
            Ticket #%d: %s
            Müşteri: %s
            Temsilci: %s
            Durum: %s
            Öncelik: %s
            Aciliyet: %s
            Yaş: %s
            """.formatted(
                id,
                getDisplayTitle(),
                customer != null ? customer.getName() : "Bilinmeyen",
                agent != null ? agent.getName() : "Atanmamış",
                getStatusWithIcon(),
                getPriorityWithIcon(),
                getUrgency().getFormattedName(),
                getAgeFormatted()
        );
    }
}
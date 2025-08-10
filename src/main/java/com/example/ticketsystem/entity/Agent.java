package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor

@Table(name = "agents", indexes = {
        @Index(name = "idx_agent_email", columnList = "email"),
        @Index(name = "idx_agent_name", columnList = "name"),
        @Index(name = "idx_agent_active", columnList = "is_active"),
        @Index(name = "idx_agent_created_at", columnList = "created_at"),
        @Index(name = "idx_agent_active_name", columnList = "is_active, name")
})
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "İsim boş olamaz")
    @Size(min = 2, max = 100, message = "İsim 2-100 karakter arasında olmalıdır")
    @Column(nullable = false, length = 100)
    private String name;

    @Email(message = "Geçerli bir email adresi giriniz")
    @NotBlank(message = "Email boş olamaz")
    @Size(max = 100, message = "Email 100 karakterden fazla olamaz")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 20, message = "Telefon numarası 20 karakterden fazla olamaz")
    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Agent() {}


    public Agent(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isActive = true;
    }


    public Agent(String name, String email, String phone, Boolean isActive) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive != null ? isActive : true;
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
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isNewAgent() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    public String getDisplayName() {
        return name != null ? name : "Unnamed Agent";
    }

    public String getMaskedEmail() {
        if (email == null || !email.contains("@")) return "***";
        var parts = email.split("@");
        var localPart = parts[0];
        var domain = parts[1];

        if (localPart.length() <= 2) {
            return "***@" + domain;
        }

        return localPart.substring(0, 2) + "***@" + domain;
    }

    public String getFormattedPhone() {
        if (phone == null || phone.length() < 10) return phone;


        return phone.replaceAll("(\\d{4})(\\d{3})(\\d{2})(\\d{2})", "$1 $2 $3 $4");
    }

    public String getStatusText() {
        return isActive() ? "Aktif" : "Pasif";
    }

    public String getStatusEmoji() {
        return isActive() ? "🟢" : "🔴";
    }


    public AgentStatus getAgentStatus() {
        if (!isActive()) {
            return AgentStatus.INACTIVE;
        }

        if (isNewAgent()) {
            return AgentStatus.NEW;
        }

        return AgentStatus.ACTIVE;
    }


    public enum AgentStatus {
        ACTIVE("Aktif", "🟢"),
        INACTIVE("Pasif", "🔴"),
        NEW("Yeni", "🆕");

        private final String displayName;
        private final String emoji;

        AgentStatus(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }

        @Override
        public String toString() {
            return emoji + " " + displayName;
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim().replaceAll("[^0-9+]", "") : null;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
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


    @Override
    public boolean equals(Object obj) {
        return obj instanceof Agent agent &&
                Objects.equals(id, agent.id) &&
                Objects.equals(email, agent.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }


    @Override
    public String toString() {
        return "Agent{id=%d, name='%s', email='%s', phone='%s', isActive=%s, createdAt=%s}"
                .formatted(id, name, getMaskedEmail(), phone, isActive, createdAt);
    }


    public String toJson() {
        return """
            {
                "id": %d,
                "name": "%s",
                "email": "%s",
                "phone": "%s",
                "isActive": %s,
                "status": "%s",
                "createdAt": "%s",
                "updatedAt": "%s",
                "isNewAgent": %s
            }
            """.formatted(
                id,
                name,
                getMaskedEmail(),
                phone,
                isActive,
                getAgentStatus(),
                createdAt,
                updatedAt,
                isNewAgent()
        );
    }


    public static AgentBuilder builder() {
        return new AgentBuilder();
    }

    public static class AgentBuilder {
        private String name;
        private String email;
        private String phone;
        private Boolean isActive = true;
        private LocalDateTime createdAt;

        public AgentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AgentBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AgentBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public AgentBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public AgentBuilder active() {
            this.isActive = true;
            return this;
        }

        public AgentBuilder inactive() {
            this.isActive = false;
            return this;
        }

        public AgentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Agent build() {
            var agent = new Agent(name, email, phone, isActive);
            if (createdAt != null) {
                agent.setCreatedAt(createdAt);
            }
            return agent;
        }
    }


    public static Agent createActiveAgent(String name, String email, String phone) {
        return new Agent(name, email, phone, true);
    }

    public static Agent createInactiveAgent(String name, String email, String phone) {
        return new Agent(name, email, phone, false);
    }


    public void toggleStatus() {
        this.isActive = !this.isActive();
        this.updatedAt = LocalDateTime.now();
    }


    public boolean canAcceptNewTickets() {
        return isActive();
    }
}
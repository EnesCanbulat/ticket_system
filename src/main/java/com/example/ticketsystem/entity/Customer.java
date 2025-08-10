package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_name", columnList = "name"),
        @Index(name = "idx_customer_created_at", columnList = "created_at")
})
public class Customer {

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;




    public Customer() {}

    // Constructor with parameters
    public Customer(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }


    public Customer(String name, String email, String phone, LocalDateTime createdAt) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
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


    public boolean isRecentCustomer() {
        if (createdAt == null) return false;
        return createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }

    public String getDisplayName() {
        return name != null ? name : "Unnamed Customer";
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

    // Enhanced equals and hashCode with modern Java
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Customer customer &&
                Objects.equals(id, customer.id) &&
                Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }


    @Override
    public String toString() {
        return "Customer{id=%d, name='%s', email='%s', phone='%s', createdAt=%s}"
                .formatted(id, name, getMaskedEmail(), phone, createdAt);
    }


    public String toJson() {
        return """
            {
                "id": %d,
                "name": "%s",
                "email": "%s",
                "phone": "%s",
                "createdAt": "%s",
                "updatedAt": "%s",
                "isRecentCustomer": %s
            }
            """.formatted(
                id,
                name,
                getMaskedEmail(),
                phone,
                createdAt,
                updatedAt,
                isRecentCustomer()
        );
    }


    public static CustomerBuilder builder() {
        return new CustomerBuilder();
    }

    public static class CustomerBuilder {
        private String name;
        private String email;
        private String phone;
        private LocalDateTime createdAt;

        public CustomerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CustomerBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CustomerBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public CustomerBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Customer build() {
            var customer = new Customer(name, email, phone);
            if (createdAt != null) {
                customer.setCreatedAt(createdAt);
            }
            return customer;
        }
    }
}
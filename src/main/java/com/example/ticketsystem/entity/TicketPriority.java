package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity
@Table(name = "ticket_priorities", indexes = {
        @Index(name = "idx_priority_name", columnList = "name"),
        @Index(name = "idx_priority_level", columnList = "level")
})
public class TicketPriority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Öncelik adı boş olamaz")
    @Size(min = 2, max = 50, message = "Öncelik adı 2-50 karakter arasında olmalıdır")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @NotNull(message = "Öncelik seviyesi gerekli")
    @Min(value = 1, message = "Öncelik seviyesi minimum 1 olmalıdır")
    @Max(value = 10, message = "Öncelik seviyesi maximum 10 olmalıdır")
    @Column(nullable = false, unique = true)
    private Integer level;


    public static final String LOW = "Düşük";
    public static final String NORMAL = "Normal";
    public static final String HIGH = "Yüksek";
    public static final String URGENT = "Acil";
    public static final String CRITICAL = "Kritik";


    public TicketPriority() {}

    public TicketPriority(String name, Integer level) {
        this.name = name;
        this.level = level;
    }


    public boolean isLow() {
        return LOW.equals(name) || (level != null && level <= 2);
    }

    public boolean isNormal() {
        return NORMAL.equals(name) || (level != null && level >= 3 && level <= 4);
    }

    public boolean isHigh() {
        return HIGH.equals(name) || (level != null && level >= 5 && level <= 7);
    }

    public boolean isUrgent() {
        return URGENT.equals(name) || (level != null && level >= 8 && level <= 9);
    }

    public boolean isCritical() {
        return CRITICAL.equals(name) || (level != null && level == 10);
    }

    public String getPriorityCategory() {
        return switch (level) {
            case 1, 2 -> "Düşük";
            case 3, 4 -> "Normal";
            case 5, 6, 7 -> "Yüksek";
            case 8, 9 -> "Acil";
            case 10 -> "Kritik";
            default -> "Bilinmeyen";
        };
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

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TicketPriority priority &&
                Objects.equals(id, priority.id) &&
                Objects.equals(level, priority.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level);
    }

    @Override
    public String toString() {
        return "TicketPriority{id=%d, name='%s', level=%d}"
                .formatted(id, name, level);
    }
}

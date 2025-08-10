package com.example.ticketsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_messages",
        indexes = {
                @Index(name = "idx_ticket_message_ticket_id", columnList = "ticket_id"),
                @Index(name = "idx_ticket_message_sender", columnList = "sender_type, sender_id"),
                @Index(name = "idx_ticket_message_created_at", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ticket_message_ticket"))
    @NotNull(message = "Ticket gerekli")
    private Ticket ticket;


    @Column(name = "sender_id", nullable = false)
    @NotNull(message = "Gönderen ID'si gerekli")
    private Long senderId;


    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    @NotNull(message = "Gönderen tipi gerekli")
    private SenderType senderType;


    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "Mesaj içeriği boş olamaz")
    @Size(min = 1, max = 10000, message = "Mesaj 1-10000 karakter arasında olmalıdır")
    private String message;


    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 30)
    private MessageType messageType = MessageType.NORMAL;


    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private MessagePriority priority = MessagePriority.NORMAL;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "read_at")
    private LocalDateTime readAt;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    @Column(name = "internal_notes", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Internal notlar 2000 karakteri geçemez")
    private String internalNotes;


    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


    @Column(name = "reply_to_message_id")
    private Long replyToMessageId;


    public enum SenderType {
        CUSTOMER("Müşteri"),
        AGENT("Temsilci"),
        SYSTEM("Sistem"),
        ADMIN("Yönetici");

        private final String displayName;

        SenderType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    public enum MessageType {
        NORMAL("Normal Mesaj"),
        STATUS_UPDATE("Durum Güncelleme"),
        ASSIGNMENT("Atama"),
        ESCALATION("Yükseltme"),
        RESOLUTION("Çözüm"),
        FOLLOW_UP("Takip"),
        INTERNAL("İç Mesaj"),
        SYSTEM_NOTIFICATION("Sistem Bildirimi");

        private final String displayName;

        MessageType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }


    public enum MessagePriority {
        LOW("Düşük"),
        NORMAL("Normal"),
        HIGH("Yüksek"),
        URGENT("Acil");

        private final String displayName;

        MessagePriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // --- Business Methods ---

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }


    public boolean isRead() {
        return this.readAt != null;
    }


    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isActive = false;
    }


    public boolean isDeleted() {
        return this.deletedAt != null || !Boolean.TRUE.equals(this.isActive);
    }


    public boolean isReply() {
        return this.replyToMessageId != null;
    }


    public boolean isSystemMessage() {
        return SenderType.SYSTEM.equals(this.senderType);
    }


    public boolean isInternalMessage() {
        return MessageType.INTERNAL.equals(this.messageType);
    }


    public int getMessageLength() {
        return this.message != null ? this.message.length() : 0;
    }


    public String getMessageSummary() {
        if (this.message == null || this.message.isEmpty()) {
            return "";
        }
        return this.message.length() > 100
                ? this.message.substring(0, 100) + "..."
                : this.message;
    }




    public TicketMessage(Ticket ticket, Long senderId, SenderType senderType, String message) {
        this.ticket = ticket;
        this.senderId = senderId;
        this.senderType = senderType;
        this.message = message;
        this.messageType = MessageType.NORMAL;
        this.priority = MessagePriority.NORMAL;
        this.isActive = true;
    }


    public static TicketMessage createSystemMessage(Ticket ticket, String message) {
        return new TicketMessage(
                ticket,
                0L,
                SenderType.SYSTEM,
                message
        );
    }


    public static TicketMessage createInternalMessage(Ticket ticket, Long agentId, String message) {
        TicketMessage ticketMessage = new TicketMessage(ticket, agentId, SenderType.AGENT, message);
        ticketMessage.setMessageType(MessageType.INTERNAL);
        return ticketMessage;
    }

    @Override
    public String toString() {
        return String.format("TicketMessage{id=%d, ticketId=%d, senderType=%s, messageLength=%d, createdAt=%s}",
                id,
                ticket != null ? ticket.getId() : null,
                senderType,
                getMessageLength(),
                createdAt
        );
    }
}
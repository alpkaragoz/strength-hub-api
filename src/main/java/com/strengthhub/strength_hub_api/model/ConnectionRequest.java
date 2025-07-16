package com.strengthhub.strength_hub_api.model;

import com.strengthhub.strength_hub_api.enums.ConnectionRequestStatus;
import com.strengthhub.strength_hub_api.enums.ConnectionRequestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "connection_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConnectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionRequestType type; // COACH_TO_LIFTER, LIFTER_TO_COACH

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConnectionRequestStatus status = ConnectionRequestStatus.PENDING;

    @Column(length = 500)
    private String message; // Optional message from sender

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime respondedAt;

    @Column
    private String responseMessage; // Optional response message

    // Helper methods
    public boolean isPending() {
        return status == ConnectionRequestStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == ConnectionRequestStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == ConnectionRequestStatus.REJECTED;
    }

    public void accept(String responseMessage) {
        this.status = ConnectionRequestStatus.ACCEPTED;
        this.responseMessage = responseMessage;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject(String responseMessage) {
        this.status = ConnectionRequestStatus.REJECTED;
        this.responseMessage = responseMessage;
        this.respondedAt = LocalDateTime.now();
    }
}
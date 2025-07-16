package com.strengthhub.strength_hub_api.repository;

import com.strengthhub.strength_hub_api.enums.ConnectionRequestStatus;
import com.strengthhub.strength_hub_api.enums.ConnectionRequestType;
import com.strengthhub.strength_hub_api.model.ConnectionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, UUID> {

    // Find requests sent by a user
    List<ConnectionRequest> findBySender_UserIdOrderByCreatedAtDesc(UUID senderId);

    // Find requests received by a user
    List<ConnectionRequest> findByReceiver_UserIdOrderByCreatedAtDesc(UUID receiverId);

    // Find pending requests received by a user
    List<ConnectionRequest> findByReceiver_UserIdAndStatusOrderByCreatedAtDesc(UUID receiverId, ConnectionRequestStatus status);

    // Find pending requests sent by a user
    List<ConnectionRequest> findBySender_UserIdAndStatusOrderByCreatedAtDesc(UUID senderId, ConnectionRequestStatus status);

    // Check if there's already a pending request between two users
    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
            "((cr.sender.userId = :userId1 AND cr.receiver.userId = :userId2) OR " +
            "(cr.sender.userId = :userId2 AND cr.receiver.userId = :userId1)) AND " +
            "cr.status = :status")
    Optional<ConnectionRequest> findPendingRequestBetweenUsers(@Param("userId1") UUID userId1,
                                                               @Param("userId2") UUID userId2,
                                                               @Param("status") ConnectionRequestStatus status);

    // Check if there's any request between two users
    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
            "((cr.sender.userId = :userId1 AND cr.receiver.userId = :userId2) OR " +
            "(cr.sender.userId = :userId2 AND cr.receiver.userId = :userId1)) " +
            "ORDER BY cr.createdAt DESC")
    List<ConnectionRequest> findRequestsBetweenUsers(@Param("userId1") UUID userId1,
                                                     @Param("userId2") UUID userId2);

    // Find requests by type
    List<ConnectionRequest> findByTypeAndStatusOrderByCreatedAtDesc(ConnectionRequestType type, ConnectionRequestStatus status);

    // Count pending requests for a user
    Long countByReceiver_UserIdAndStatus(UUID receiverId, ConnectionRequestStatus status);

    // Find accepted requests where user is involved (to determine current connections)
    @Query("SELECT cr FROM ConnectionRequest cr WHERE " +
            "((cr.sender.userId = :userId OR cr.receiver.userId = :userId) AND " +
            "cr.status = 'ACCEPTED') ORDER BY cr.respondedAt DESC")
    List<ConnectionRequest> findAcceptedRequestsByUser(@Param("userId") UUID userId);
}
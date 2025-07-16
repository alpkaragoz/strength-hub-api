package com.strengthhub.strength_hub_api.service;

import com.strengthhub.strength_hub_api.dto.request.connection.ConnectionRequestCreateRequest;
import com.strengthhub.strength_hub_api.dto.request.connection.ConnectionRequestResponseRequest;
import com.strengthhub.strength_hub_api.dto.response.connection.ConnectionRequestResponse;
import com.strengthhub.strength_hub_api.enums.ConnectionRequestStatus;
import com.strengthhub.strength_hub_api.enums.ConnectionRequestType;
import com.strengthhub.strength_hub_api.exception.common.UnauthorizedAccessException;
import com.strengthhub.strength_hub_api.exception.connection.ConnectionRequestNotFoundException;
import com.strengthhub.strength_hub_api.exception.connection.DuplicateConnectionRequestException;
import com.strengthhub.strength_hub_api.exception.connection.InvalidConnectionRequestException;
import com.strengthhub.strength_hub_api.exception.user.UserNotFoundException;
import com.strengthhub.strength_hub_api.model.ConnectionRequest;
import com.strengthhub.strength_hub_api.model.User;
import com.strengthhub.strength_hub_api.repository.ConnectionRequestRepository;
import com.strengthhub.strength_hub_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionRequestService {

    private final ConnectionRequestRepository connectionRequestRepository;
    private final UserRepository userRepository;
    private final CoachService coachService;

    @Transactional
    public ConnectionRequestResponse sendConnectionRequest(UUID senderId, ConnectionRequestCreateRequest request) {
        log.info("User {} sending connection request to user {}", senderId, request.getReceiverId());

        // Validate users exist
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException(senderId.toString()));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new UserNotFoundException(request.getReceiverId().toString()));

        // Can't send request to yourself
        if (senderId.equals(request.getReceiverId())) {
            throw new InvalidConnectionRequestException("Cannot send connection request to yourself");
        }

        // Check if there's already a pending request between these users
        connectionRequestRepository.findPendingRequestBetweenUsers(senderId, request.getReceiverId(), ConnectionRequestStatus.PENDING)
                .ifPresent(existingRequest -> {
                    throw new DuplicateConnectionRequestException("There is already a pending request between these users");
                });

        // Determine request type
        ConnectionRequestType type = determineRequestType(sender, receiver);

        // Create connection request
        ConnectionRequest connectionRequest = ConnectionRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .type(type)
                .message(request.getMessage())
                .status(ConnectionRequestStatus.PENDING)
                .build();

        ConnectionRequest savedRequest = connectionRequestRepository.save(connectionRequest);
        log.info("Connection request created with id: {}", savedRequest.getRequestId());

        return mapToResponse(savedRequest);
    }

    @Transactional
    public ConnectionRequestResponse respondToConnectionRequest(UUID requestId, UUID responderId, ConnectionRequestResponseRequest request) {
        log.info("User {} responding to connection request {}", responderId, requestId);

        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ConnectionRequestNotFoundException("Connection request not found"));

        // Verify the responder is the receiver
        if (!connectionRequest.getReceiver().getUserId().equals(responderId)) {
            throw new UnauthorizedAccessException("You can only respond to requests sent to you");
        }

        // Verify request is still pending
        if (!connectionRequest.isPending()) {
            throw new ConnectionRequestNotFoundException("This request has already been responded to");
        }

        // Validate status
        if (request.getStatus() != ConnectionRequestStatus.ACCEPTED && request.getStatus() != ConnectionRequestStatus.REJECTED) {
            throw new InvalidConnectionRequestException("Status must be ACCEPTED or REJECTED");
        }

        // Update request status
        if (request.getStatus() == ConnectionRequestStatus.ACCEPTED) {
            connectionRequest.accept(request.getResponseMessage());
            // Create the actual coach-lifter relationship
            createCoachLifterRelationship(connectionRequest);
        } else {
            connectionRequest.reject(request.getResponseMessage());
        }

        ConnectionRequest updatedRequest = connectionRequestRepository.save(connectionRequest);
        log.info("Connection request {} {}", requestId, request.getStatus().name().toLowerCase());

        return mapToResponse(updatedRequest);
    }

    @Transactional
    public void cancelConnectionRequest(UUID requestId, UUID senderId) {
        log.info("User {} canceling connection request {}", senderId, requestId);

        ConnectionRequest connectionRequest = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ConnectionRequestNotFoundException("Connection request not found"));

        // Verify the canceller is the sender
        if (!connectionRequest.getSender().getUserId().equals(senderId)) {
            throw new UnauthorizedAccessException("You can only cancel requests you sent");
        }

        // Verify request is still pending
        if (!connectionRequest.isPending()) {
            throw new ConnectionRequestNotFoundException("This request has already been responded to");
        }

        connectionRequest.setStatus(ConnectionRequestStatus.CANCELLED);
        connectionRequestRepository.save(connectionRequest);
        log.info("Connection request {} cancelled", requestId);
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getSentRequests(UUID userId) {
        log.info("Fetching sent requests for user {}", userId);

        return connectionRequestRepository.findBySender_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getReceivedRequests(UUID userId) {
        log.info("Fetching received requests for user {}", userId);

        return connectionRequestRepository.findByReceiver_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConnectionRequestResponse> getPendingReceivedRequests(UUID userId) {
        log.info("Fetching pending received requests for user {}", userId);

        return connectionRequestRepository.findByReceiver_UserIdAndStatusOrderByCreatedAtDesc(userId, ConnectionRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getPendingRequestCount(UUID userId) {
        return connectionRequestRepository.countByReceiver_UserIdAndStatus(userId, ConnectionRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public ConnectionRequestResponse getConnectionRequestById(UUID requestId) {
        ConnectionRequest request = connectionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ConnectionRequestNotFoundException("Connection request not found"));

        return mapToResponse(request);
    }

    private ConnectionRequestType determineRequestType(User sender, User receiver) {
        boolean senderIsCoach = sender.isCoach();
        boolean receiverIsCoach = receiver.isCoach();
        boolean senderIsLifter = sender.isLifter();
        boolean receiverIsLifter = receiver.isLifter();

        // If sender is coach and receiver is lifter (or lifter-only)
        if (senderIsCoach && receiverIsLifter) {
            return ConnectionRequestType.COACH_TO_LIFTER;
        }

        // If sender is lifter and receiver is coach (or coach-only)
        if (senderIsLifter && receiverIsCoach) {
            return ConnectionRequestType.LIFTER_TO_COACH;
        }

        // If both have same roles, default to coach-to-lifter if sender is coach
        if (senderIsCoach) {
            return ConnectionRequestType.COACH_TO_LIFTER;
        }

        return ConnectionRequestType.LIFTER_TO_COACH;
    }

    private void createCoachLifterRelationship(ConnectionRequest request) {
        User sender = request.getSender();
        User receiver = request.getReceiver();

        UUID coachId = null;
        UUID lifterId = null;

        if (request.getType() == ConnectionRequestType.COACH_TO_LIFTER) {
            coachId = sender.getUserId();
            lifterId = receiver.getUserId();
        } else {
            coachId = receiver.getUserId();
            lifterId = sender.getUserId();
        }

        try {
            coachService.assignLifterToCoach(coachId, lifterId);
            log.info("Coach-lifter relationship created: coach={}, lifter={}", coachId, lifterId);
        } catch (Exception e) {
            log.error("Failed to create coach-lifter relationship: {}", e.getMessage());
            throw new InvalidConnectionRequestException("Failed to create coach-lifter relationship");
        }
    }

    private ConnectionRequestResponse mapToResponse(ConnectionRequest request) {
        User sender = request.getSender();
        User receiver = request.getReceiver();

        return ConnectionRequestResponse.builder()
                .requestId(request.getRequestId())
                .senderId(sender.getUserId())
                .senderName(sender.getFirstName() + " " + sender.getLastName())
                .senderUsername(sender.getUsername())
                .receiverId(receiver.getUserId())
                .receiverName(receiver.getFirstName() + " " + receiver.getLastName())
                .receiverUsername(receiver.getUsername())
                .type(request.getType())
                .status(request.getStatus())
                .message(request.getMessage())
                .responseMessage(request.getResponseMessage())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}
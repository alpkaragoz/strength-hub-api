package com.strengthhub.strength_hub_api.controller;

import com.strengthhub.strength_hub_api.dto.request.connection.ConnectionRequestCreateRequest;
import com.strengthhub.strength_hub_api.dto.request.connection.ConnectionRequestResponseRequest;
import com.strengthhub.strength_hub_api.dto.response.connection.ConnectionRequestResponse;
import com.strengthhub.strength_hub_api.security.SecurityUtils;
import com.strengthhub.strength_hub_api.service.ConnectionRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/connection-requests")
@RequiredArgsConstructor
@Validated
public class ConnectionRequestController {

    private final ConnectionRequestService connectionRequestService;

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConnectionRequestResponse> sendConnectionRequest(@Valid @RequestBody ConnectionRequestCreateRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ConnectionRequestResponse response = connectionRequestService.sendConnectionRequest(currentUserId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{requestId}/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConnectionRequestResponse> respondToConnectionRequest(@PathVariable UUID requestId,
                                                                                @Valid @RequestBody ConnectionRequestResponseRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        ConnectionRequestResponse response = connectionRequestService.respondToConnectionRequest(requestId, currentUserId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelConnectionRequest(@PathVariable UUID requestId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        connectionRequestService.cancelConnectionRequest(requestId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConnectionRequestResponse>> getSentRequests() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<ConnectionRequestResponse> requests = connectionRequestService.getSentRequests(currentUserId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConnectionRequestResponse>> getReceivedRequests() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<ConnectionRequestResponse> requests = connectionRequestService.getReceivedRequests(currentUserId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConnectionRequestResponse>> getPendingReceivedRequests() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        List<ConnectionRequestResponse> requests = connectionRequestService.getPendingReceivedRequests(currentUserId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/pending/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getPendingRequestCount() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Long count = connectionRequestService.getPendingRequestCount(currentUserId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConnectionRequestResponse> getConnectionRequestById(@PathVariable UUID requestId) {
        ConnectionRequestResponse request = connectionRequestService.getConnectionRequestById(requestId);
        return ResponseEntity.ok(request);
    }
}
package com.user.provisioning.controller;

import com.user.provisioning.dto.MembershipRequest;
import com.user.provisioning.dto.MessageResponse;
import com.user.provisioning.entity.Membership;
import com.user.provisioning.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/membership-requests")
@RequiredArgsConstructor
@Slf4j
public class MembershipRequestController {

    private final MembershipService membershipService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Membership>> getAllRequests() {
        log.info("Fetching all membership requests");
        List<Membership> requests = membershipService.getAllRequests();
        log.info("Fetched {} membership requests", requests.size());
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Membership> getRequestById(@PathVariable Long id) {
        log.info("Fetching membership request with id: {}", id);
        Membership request = membershipService.getRequestById(id);
        log.info("Fetched membership request: {}", request);
        return ResponseEntity.ok(request);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Membership> createRequest(@RequestBody MembershipRequest request) {
        log.info("Creating new membership request for employee: {}", request.getEmployeeId());
        Membership createdRequest = membershipService.createRequest(request);
        log.info("Created membership request: {}", createdRequest);
        return ResponseEntity.ok(createdRequest);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MessageResponse> deleteRequest(@PathVariable Long id) {
        log.info("Deleting membership request with id: {}", id);
        membershipService.deleteRequest(id);
        log.info("Deleted membership request with id: {}", id);
        return ResponseEntity.ok(new MessageResponse("Membership request record has been deleted successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Membership> approveRequest(@PathVariable Long id) {
        log.info("Approving membership request with id: {}", id);
        Membership approvedRequest = membershipService.approveRequest(id);
        log.info("Approved membership request: {}", approvedRequest);
        return ResponseEntity.ok(approvedRequest);
    }

    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Membership> denyRequest(@PathVariable Long id) {
        log.info("Denying membership request with id: {}", id);
        Membership deniedRequest = membershipService.denyRequest(id);
        log.info("Denied membership request: {}", deniedRequest);
        return ResponseEntity.ok(deniedRequest);
    }
}

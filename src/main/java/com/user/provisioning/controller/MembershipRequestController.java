package com.user.provisioning.controller;

import com.user.provisioning.entity.MembershipRequest;
import com.user.provisioning.service.MembershipRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/membership-requests")
@RequiredArgsConstructor
public class MembershipRequestController {

    private final MembershipRequestService membershipRequestService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MembershipRequest>> getAllRequests() {
        return ResponseEntity.ok(membershipRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MembershipRequest> getRequestById(@PathVariable String id) {
        MembershipRequest request = membershipRequestService.getRequestById(id);
        if (request != null) {
            return ResponseEntity.ok(request);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public MembershipRequest createRequest(@RequestBody MembershipRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return membershipRequestService.createRequest(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MembershipRequest> updateRequest(@PathVariable String id, @RequestBody MembershipRequest requestDetails) {
        MembershipRequest updatedRequest = membershipRequestService.updateRequest(id, requestDetails);
        if (updatedRequest != null) {
            return ResponseEntity.ok(updatedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        membershipRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipRequest> approveRequest(@PathVariable String id, @RequestParam String reviewedBy) {
        MembershipRequest approvedRequest = membershipRequestService.approveRequest(id, reviewedBy);
        if (approvedRequest != null) {
            return ResponseEntity.ok(approvedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/deny")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembershipRequest> denyRequest(@PathVariable String id, @RequestParam String reviewedBy) {
        MembershipRequest deniedRequest = membershipRequestService.denyRequest(id, reviewedBy);
        if (deniedRequest != null) {
            return ResponseEntity.ok(deniedRequest);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

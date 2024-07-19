package com.user.provisioning.service;

import com.user.provisioning.entity.ERole;
import com.user.provisioning.entity.MembershipRequest;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.MembershipRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MembershipRequestService {

    private final MembershipRequestRepository membershipRequestRepository;

    public List<MembershipRequest> getAllRequests() {
        return membershipRequestRepository.findAll();
    }

    public MembershipRequest getRequestById(String id) {
        Optional<MembershipRequest> optionalMembershipRequest = membershipRequestRepository.findById(id);
        if(optionalMembershipRequest.isPresent()) {
            return optionalMembershipRequest.get();
        }
        throw new UserProvisioningCustomException(ErrorCode.MEMBERSHIP_REQUEST_NOT_FOUND);
    }

    public MembershipRequest createRequest(MembershipRequest request) {
        if(isAdmin()) {

        }
        request.setStatus("Pending");
        return membershipRequestRepository.save(request);
    }

    public MembershipRequest updateRequest(String id, MembershipRequest requestDetails) {
        MembershipRequest request = membershipRequestRepository.findById(id).orElse(null);
        if (request != null) {
            request.setStatus(requestDetails.getStatus());
            request.setRequestedBy(requestDetails.getRequestedBy());
            request.setReviewedBy(requestDetails.getReviewedBy());
            return membershipRequestRepository.save(request);
        }
        return null;
    }

    public void deleteRequest(String id) {
        membershipRequestRepository.deleteById(id);
    }

    public MembershipRequest approveRequest(String id, String reviewedBy) {
        MembershipRequest request = membershipRequestRepository.findById(id).orElse(null);
        if (request != null && "Pending".equals(request.getStatus())) {
            request.setStatus("Approved");
            request.setReviewedBy(reviewedBy);
            return membershipRequestRepository.save(request);
        }
        return null;
    }

    public MembershipRequest denyRequest(String id, String reviewedBy) {
        MembershipRequest request = membershipRequestRepository.findById(id).orElse(null);
        if (request != null && "Pending".equals(request.getStatus())) {
            request.setStatus("Denied");
            request.setReviewedBy(reviewedBy);
            return membershipRequestRepository.save(request);
        }
        return null;
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> authorities = ((List<GrantedAuthority>) userDetails.getAuthorities()).stream().map(GrantedAuthority::getAuthority).toList();
        return authorities.contains(ERole.ROLE_ADMIN.name());
    }
}

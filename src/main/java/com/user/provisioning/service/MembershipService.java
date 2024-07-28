package com.user.provisioning.service;

import com.user.provisioning.dto.MembershipRequest;
import com.user.provisioning.enums.ApprovalStatus;
import com.user.provisioning.enums.ERole;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.entity.Membership;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * Service class for managing membership-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final EmployeeService employeeService;
    private final GroupService groupService;

    /**
     * Retrieves all membership requests.
     *
     * @return a list of all membership requests
     */
    public List<Membership> getAllRequests() {
        log.info("Fetching all membership requests");
        return membershipRepository.findAll();
    }

    /**
     * Retrieves a membership request by its ID.
     *
     * @param id the ID of the membership request
     * @return the membership request with the specified ID
     * @throws UserProvisioningCustomException if the membership request is not found or the user is not the valid owner
     */
    public Membership getRequestById(Long id) {
        log.info("Fetching membership request with id: {}", id);
        Membership membershipRequest = membershipRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Membership request with id {} not found", id);
                    return new UserProvisioningCustomException(ErrorCode.MEMBERSHIP_REQUEST_NOT_FOUND);
                });
        if (validateOwner(membershipRequest) || isAdmin()) {
            return membershipRequest;
        } else {
            log.error("User is not valid owner of membership request with id {}", id);
            throw new UserProvisioningCustomException(ErrorCode.NOT_VALID_OWNER_OF_REQUEST);
        }
    }

    /**
     * Creates a new membership request.
     *
     * @param request the request object containing membership details
     * @return the created membership request
     */
    public Membership createRequest(MembershipRequest request) {
        log.info("Creating membership request for employee id: {} and group id: {}", request.getEmployeeId(), request.getGroupId());
        Membership membership = request.getMembership();
        if (!isAdmin() || isNull(membership.getRequestedBy()) || membership.getRequestedBy().isEmpty()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            membership.setRequestedBy(userDetails.getUsername());
        }
        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        Group group = groupService.getGroupById(request.getGroupId());
        membership.setEmployee(employee);
        membership.setGroup(group);
        Membership savedMembership = membershipRepository.save(membership);
        log.info("Membership request with id {} created successfully", savedMembership.getId());
        return savedMembership;
    }

    /**
     * Deletes a membership request by its ID.
     *
     * @param id the ID of the membership request to delete
     * @throws UserProvisioningCustomException if the membership request is not found
     */
    public void deleteRequest(Long id) {
        log.info("Deleting membership request with id: {}", id);
        getRequestById(id);
        membershipRepository.deleteById(id);
        log.info("Membership request with id {} deleted successfully", id);
    }

    /**
     * Approves a membership request.
     *
     * @param id the ID of the membership request to approve
     * @return the approved membership request
     * @throws UserProvisioningCustomException if the request status is invalid for approval
     */
    public Membership approveRequest(Long id) {
        log.info("Approving membership request with id: {}", id);
        Membership request = getRequestById(id);
        if (ApprovalStatus.PENDING.name().equals(request.getStatus())) {
            request.setStatus(ApprovalStatus.APPROVED.name());
            request.setReviewedBy(fetchReviewer());
            Membership approvedRequest = membershipRepository.save(request);
            log.info("Membership request with id {} approved", approvedRequest.getId());
            return approvedRequest;
        } else {
            log.error("Invalid request status for approval with id {}", id);
            throw new UserProvisioningCustomException(ErrorCode.INVALID_REQUEST_STATUS);
        }
    }

    /**
     * Denies a membership request.
     *
     * @param id the ID of the membership request to deny
     * @return the denied membership request
     * @throws UserProvisioningCustomException if the request status is invalid for denial
     */
    public Membership denyRequest(Long id) {
        log.info("Denying membership request with id: {}", id);
        Membership request = getRequestById(id);
        if (ApprovalStatus.PENDING.name().equals(request.getStatus())) {
            request.setStatus(ApprovalStatus.DENIED.name());
            request.setReviewedBy(fetchReviewer());
            Membership deniedRequest = membershipRepository.save(request);
            log.info("Membership request with id {} denied", deniedRequest.getId());
            return deniedRequest;
        } else {
            log.error("Invalid request status for denial with id {}", id);
            throw new UserProvisioningCustomException(ErrorCode.INVALID_REQUEST_STATUS);
        }
    }

    /**
     * Checks if the currently authenticated user has admin role.
     *
     * @return true if the user has admin role, false otherwise
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(ERole.ROLE_ADMIN.name()));
    }

    /**
     * Validates if the currently authenticated user is the owner of the membership request.
     *
     * @param request the membership request to validate
     * @return true if the user is the owner, false otherwise
     */
    private boolean validateOwner(Membership request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return request.getRequestedBy().equals(userDetails.getUsername());
    }

    /**
     * Fetches the username of the currently authenticated user.
     *
     * @return the username of the currently authenticated user
     */
    private String fetchReviewer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}

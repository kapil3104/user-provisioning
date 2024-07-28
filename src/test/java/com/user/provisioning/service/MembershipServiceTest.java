package com.user.provisioning.service;

import com.user.provisioning.dto.MembershipRequest;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.entity.Membership;
import com.user.provisioning.enums.ApprovalStatus;
import com.user.provisioning.enums.ERole;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//@ExtendWith(SpringExtension.class)
public class MembershipServiceTest {

    @InjectMocks
    private MembershipService membershipService;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private GroupService groupService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testGetAllRequests() {
        List<Membership> memberships = List.of(new Membership(), new Membership());
        when(membershipRepository.findAll()).thenReturn(memberships);

        List<Membership> result = membershipService.getAllRequests();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(membershipRepository, times(1)).findAll();
    }

    @Test
    public void testGetRequestById_Success_Admin() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());

        Membership result = membershipService.getRequestById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(membershipRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetRequestById_Success_Owner() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setRequestedBy("user");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createRegularUser("user"));

        Membership result = membershipService.getRequestById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(membershipRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetRequestById_NotOwnerOrAdmin() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setRequestedBy("anotherUser");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createRegularUser("user"));

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            membershipService.getRequestById(1L);
        });

        assertEquals(ErrorCode.NOT_VALID_OWNER_OF_REQUEST, exception.getErrorCode());
        verify(membershipRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateRequest_AsAdmin() {
        MembershipRequest membershipRequest = createMembershipRequest();
        Membership membership = membershipRequest.getMembership();
        membership.setRequestedBy("admin");

        when(authentication.getPrincipal()).thenReturn(createAdminUser());
        when(employeeService.getEmployeeById(anyLong())).thenReturn(new Employee());
        when(groupService.getGroupById(anyLong())).thenReturn(new Group());
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership result = membershipService.createRequest(membershipRequest);

        assertNotNull(result);
        assertEquals("admin", result.getRequestedBy());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    public void testCreateRequest_AsUser() {
        MembershipRequest membershipRequest = createMembershipRequest();
        Membership membership = membershipRequest.getMembership();
        membership.setRequestedBy("user");

        when(authentication.getPrincipal()).thenReturn(createRegularUser("user"));
        when(employeeService.getEmployeeById(anyLong())).thenReturn(new Employee());
        when(groupService.getGroupById(anyLong())).thenReturn(new Group());
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership result = membershipService.createRequest(membershipRequest);

        assertNotNull(result);
        assertEquals("user", result.getRequestedBy());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    public void testDeleteRequest_Success() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());

        membershipService.deleteRequest(1L);

        verify(membershipRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteRequest_NotFound() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            membershipService.deleteRequest(1L);
        });

        assertEquals(ErrorCode.MEMBERSHIP_REQUEST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testApproveRequest_Success() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setStatus(ApprovalStatus.PENDING.name());
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership result = membershipService.approveRequest(1L);

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED.name(), result.getStatus());
        assertEquals("admin", result.getReviewedBy());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    public void testApproveRequest_InvalidStatus() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setStatus(ApprovalStatus.APPROVED.name());
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            membershipService.approveRequest(1L);
        });

        assertEquals(ErrorCode.INVALID_REQUEST_STATUS, exception.getErrorCode());
    }

    @Test
    public void testDenyRequest_Success() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setStatus(ApprovalStatus.PENDING.name());
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership result = membershipService.denyRequest(1L);

        assertNotNull(result);
        assertEquals("admin", result.getReviewedBy());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    public void testDenyRequest_InvalidStatus() {
        Membership membership = new Membership();
        membership.setId(1L);
        membership.setStatus(ApprovalStatus.APPROVED.name());
        membership.setRequestedBy("reviewer");
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(authentication.getPrincipal()).thenReturn(createAdminUser());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            membershipService.denyRequest(1L);
        });

        assertEquals(ErrorCode.INVALID_REQUEST_STATUS, exception.getErrorCode());
    }

    private MembershipRequest createMembershipRequest() {
        MembershipRequest request = new MembershipRequest();
        return request;
    }

    private UserDetails createAdminUser() {
        List<GrantedAuthority> authorities = List.of(() -> ERole.ROLE_ADMIN.name());
        return new User("admin", "password", authorities);
    }

    private UserDetails createRegularUser(String username) {
        List<GrantedAuthority> authorities = List.of(() -> ERole.ROLE_USER.name());
        return new User(username, "password", authorities);
    }
}

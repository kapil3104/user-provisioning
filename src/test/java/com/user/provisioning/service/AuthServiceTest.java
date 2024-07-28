package com.user.provisioning.service;

import com.user.provisioning.dto.LoginRequest;
import com.user.provisioning.dto.LoginResponse;
import com.user.provisioning.dto.SignupRequest;
import com.user.provisioning.dto.UserDetailsImpl;
import com.user.provisioning.entity.Role;
import com.user.provisioning.entity.User;
import com.user.provisioning.enums.ERole;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.RoleRepository;
import com.user.provisioning.repository.UserRepository;
import com.user.provisioning.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_EmailExists() {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            authService.registerUser(signUpRequest);
        });

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());

        verify(userRepository).existsByUsername(signUpRequest.getUsername());
        verify(userRepository).existsByEmail(signUpRequest.getEmail());
    }

    @Test
    public void testAuthenticateUser_Success() {
        String username = "testuser";
        String password = "testpassword";
        String jwt = "testjwt";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn(jwt);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail("test@example.com");

        when(userDetails.getId()).thenReturn(user.getId());
        when(userDetails.getUsername()).thenReturn(user.getUsername());
        when(userDetails.getEmail()).thenReturn(user.getEmail());
        when(userDetails.getAuthorities()).thenReturn(new ArrayList<>());

        LoginResponse response = authService.authenticateUser(loginRequest);

        assertNotNull(response);
        assertEquals(jwt, response.getToken());
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    public void testRegisterUser_UsernameExists() {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(true);

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            authService.registerUser(signUpRequest);
        });

        assertEquals(ErrorCode.USERNAME_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    public void testRegisterUser_NoRolesProvided() {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signUpRequest.getPassword())).thenReturn("encodedpassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        authService.registerUser(signUpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(signUpRequest.getUsername(), savedUser.getUsername());
        assertEquals(signUpRequest.getEmail(), savedUser.getEmail());
        assertEquals("encodedpassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(userRole));
    }

    @Test
    public void testRegisterUser_WithRoles() {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRoles(Set.of("admin", "user"));

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signUpRequest.getPassword())).thenReturn("encodedpassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        authService.registerUser(signUpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals(signUpRequest.getUsername(), savedUser.getUsername());
        assertEquals(signUpRequest.getEmail(), savedUser.getEmail());
        assertEquals("encodedpassword", savedUser.getPassword());
        assertTrue(savedUser.getRoles().contains(adminRole));
        assertTrue(savedUser.getRoles().contains(userRole));
    }

    @Test
    public void testRegisterUser_RoleNotFound() {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setRoles(Set.of("admin"));

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(false);
        when(encoder.encode(signUpRequest.getPassword())).thenReturn("encodedpassword");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            authService.registerUser(signUpRequest);
        });

        assertEquals(ErrorCode.ROLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testCreateRole_Success() {
        String roleName = "ROLE_USER";

        when(roleRepository.findByName(ERole.valueOf(roleName))).thenReturn(Optional.empty());

        Role role = new Role();
        role.setName(ERole.valueOf(roleName));

        when(roleRepository.save(any(Role.class))).thenReturn(role);

        Role createdRole = authService.createRole(roleName);

        assertNotNull(createdRole);
        assertEquals(ERole.valueOf(roleName), createdRole.getName());
    }

    @Test
    public void testCreateRole_RoleAlreadyExists() {
        String roleName = "ROLE_ADMIN";

        Role role = new Role();
        role.setName(ERole.ROLE_ADMIN);

        when(roleRepository.findByName(ERole.valueOf(roleName))).thenReturn(Optional.of(role));

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            authService.createRole(roleName);
        });

        assertEquals(ErrorCode.ROLE_ALREADY_PRESENT, exception.getErrorCode());
    }
}

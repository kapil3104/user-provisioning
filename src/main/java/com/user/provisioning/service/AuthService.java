package com.user.provisioning.service;

import com.user.provisioning.dto.LoginResponse;
import com.user.provisioning.dto.LoginRequest;
import com.user.provisioning.dto.SignupRequest;
import com.user.provisioning.dto.UserDetailsImpl;
import com.user.provisioning.enums.ERole;
import com.user.provisioning.entity.Role;
import com.user.provisioning.entity.User;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.RoleRepository;
import com.user.provisioning.repository.UserRepository;
import com.user.provisioning.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for authentication and user registration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    /**
     * Authenticates a user based on the provided login request.
     *
     * @param loginRequest the login request containing username and password
     * @return a LoginResponse containing JWT token, user details, and roles
     * @throws UserProvisioningCustomException if authentication fails
     */
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("User: {} authenticated successfully", loginRequest.getUsername());
        return new LoginResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    /**
     * Registers a new user with the provided signup request details.
     *
     * @param signUpRequest the signup request containing user details and roles
     * @throws UserProvisioningCustomException if the username or email already exists, or if the role is not found
     */
    public void registerUser(SignupRequest signUpRequest) {
        log.info("Registering user: {}", signUpRequest.getUsername());

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.warn("Username: {} already exists", signUpRequest.getUsername());
            throw new UserProvisioningCustomException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Email: {} already exists", signUpRequest.getEmail());
            throw new UserProvisioningCustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> {
                        log.error("Role USER not found");
                        return new UserProvisioningCustomException(ErrorCode.ROLE_NOT_FOUND);
                    });
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if (role.equals("admin")) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> {
                                log.error("Role ADMIN not found");
                                return new UserProvisioningCustomException(ErrorCode.ROLE_NOT_FOUND);
                            });
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> {
                                log.error("Role USER not found");
                                return new UserProvisioningCustomException(ErrorCode.ROLE_NOT_FOUND);
                            });
                    roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        log.info("User: {} registered successfully", signUpRequest.getUsername());
    }

    /**
     * Creates a new role with the provided role name.
     *
     * @param roleName the name of the role to be created
     * @return the created Role entity
     * @throws UserProvisioningCustomException if the role already exists or if the role name is invalid
     */
    public Role createRole(String roleName) {
        log.info("Creating role: {}", roleName);
        Optional<Role> userRole = roleRepository.findByName(ERole.valueOf(roleName));
        if (userRole.isPresent()) {
            log.warn("Role: {} already present", roleName);
            throw new UserProvisioningCustomException(ErrorCode.ROLE_ALREADY_PRESENT);
        }
        Role role = new Role();
        role.setName(ERole.valueOf(roleName));
        Role createdRole = roleRepository.save(role);
        log.info("Role: {} created successfully", roleName);
        return createdRole;
    }
}

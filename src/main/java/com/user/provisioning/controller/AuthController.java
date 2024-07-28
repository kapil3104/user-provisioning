package com.user.provisioning.controller;

import com.user.provisioning.dto.LoginRequest;
import com.user.provisioning.dto.LoginResponse;
import com.user.provisioning.dto.MessageResponse;
import com.user.provisioning.dto.SignupRequest;
import com.user.provisioning.entity.Role;
import com.user.provisioning.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        log.info("Received login request for user: {}", loginRequest.getUsername());
        LoginResponse response = authService.authenticateUser(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@RequestBody SignupRequest signUpRequest) {
        log.info("Received signup request for user: {}", signUpRequest.getUsername());
        authService.registerUser(signUpRequest);
        log.info("User registered successfully: {}", signUpRequest.getUsername());
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/add-role/{roleName}")
    public ResponseEntity<Role> createRole(@PathVariable String roleName) {
        log.info("Received request to create role: {}", roleName);
        Role role = authService.createRole(roleName);
        log.info("Role created successfully: {}", roleName);
        return ResponseEntity.ok(role);
    }
}

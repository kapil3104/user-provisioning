package com.user.provisioning.controller;

import com.user.provisioning.dto.LoginRequest;
import com.user.provisioning.dto.MessageResponse;
import com.user.provisioning.dto.SignupRequest;
import com.user.provisioning.entity.Role;
import com.user.provisioning.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/add-role/{roleName}")
    public ResponseEntity<Role> createRole(@PathVariable String roleName) {
        return ResponseEntity.ok(authService.createRole(roleName));
    }
}
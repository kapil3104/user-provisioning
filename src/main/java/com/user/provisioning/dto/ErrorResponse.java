package com.user.provisioning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final Integer statusCode;
    private final LocalDateTime timestamp;
}

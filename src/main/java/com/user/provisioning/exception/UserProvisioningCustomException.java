package com.user.provisioning.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProvisioningCustomException extends RuntimeException {
    private final ErrorCode errorCode;
}

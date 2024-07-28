package com.user.provisioning.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    GROUP_NOT_FOUND("GRP_001", "Group not found with given Id", HttpStatus.NOT_FOUND),

    INVALID_GROUP_REQUEST("REQ_001", "Invalid group request", HttpStatus.BAD_REQUEST),

    DATABASE_ERROR("DB_001", "Database operation failed", HttpStatus.INTERNAL_SERVER_ERROR),

    DYNAMIC_GROUP_RULE_VALIDATION_ERROR("RULE_001", "Dynamic group rules are missing in request", HttpStatus.BAD_REQUEST),
    DYNAMIC_GROUP_RULE_NOT_FOUND("RULE_002", "Dynamic group rule not found with given id", HttpStatus.NOT_FOUND),
    DYNAMIC_GROUP_RULES_NOT_FOUND("RULE_003", "Dynamic group rules not found with given group Id", HttpStatus.NOT_FOUND),
    DYNAMIC_GROUP_RULE_ALREADY_EXISTS("RULE_004", "Dynamic group rule already exist", HttpStatus.BAD_REQUEST),

    EMPLOYEE_NOT_FOUND("EMP_001", "Employee not found", HttpStatus.NOT_FOUND),
    EMPLOYEE_ALREADY_EXISTS("EMP_002", "Employee already exists with given email", HttpStatus.BAD_REQUEST),
    EMPLOYEES_NOT_FOUND_FOR_GIVEN_GROUP("EMP_003", "Employees not found for given group Id", HttpStatus.NOT_FOUND),

    USERNAME_ALREADY_EXISTS("USR_001", "User with given username already exists", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("USR_002", "User with given email already exists", HttpStatus.BAD_REQUEST),

    ROLE_ALREADY_PRESENT("ROL_001", "Role with given name already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("ROL_002", "Role with given name not found", HttpStatus.NOT_FOUND),

    MEMBERSHIP_REQUEST_NOT_FOUND("MEM_001", "Membership request not found", HttpStatus.NOT_FOUND),
    NOT_VALID_OWNER_OF_REQUEST("MEM_002", "User is not a valid owner of the request", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST_STATUS("MEM_003", "Existing status is not valid", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

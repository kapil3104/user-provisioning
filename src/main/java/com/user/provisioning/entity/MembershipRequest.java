package com.user.provisioning.entity;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "membership_requests")
@Data
public class MembershipRequest {
    @Id
    private String id;
    private String employeeId;
    private String groupId;
    private String status;
    private String requestedBy;
    private String reviewedBy;
}


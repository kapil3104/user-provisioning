package com.user.provisioning.dto;

import com.user.provisioning.entity.Membership;
import com.user.provisioning.enums.ApprovalStatus;
import lombok.Data;

@Data
public class MembershipRequest {
    private Long id;
    private Long employeeId;
    private Long groupId;
    private ApprovalStatus status;
    private String requestedBy;

    public Membership getMembership() {
        Membership membership = new Membership();
        membership.setStatus(ApprovalStatus.PENDING.name());
        return membership;
    }
}

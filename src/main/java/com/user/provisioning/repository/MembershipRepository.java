package com.user.provisioning.repository;

import com.user.provisioning.entity.Membership;
import com.user.provisioning.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<List<Membership>> findByGroupIdAndStatus(Long groupId, String status);
}


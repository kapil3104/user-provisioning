package com.user.provisioning.repository;

import com.user.provisioning.entity.MembershipRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRequestRepository extends MongoRepository<MembershipRequest, String> {
    Optional<List<MembershipRequest>> findByGroupIdAndStatus(String groupId, String status);

    Optional<MembershipRequest> findByEmployeeIdAndGroupId(String employeeId, String groupId);
}


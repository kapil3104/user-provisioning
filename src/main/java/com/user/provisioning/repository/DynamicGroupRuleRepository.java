package com.user.provisioning.repository;

import com.user.provisioning.entity.DynamicGroupRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicGroupRuleRepository extends MongoRepository<DynamicGroupRule, String> {
    Optional<List<DynamicGroupRule>> findByGroupId(String groupId);
    Optional<DynamicGroupRule> findOneByGroupIdAndAttributeAndOperationAndValue(String groupId, String attribute, String operation, String value);
}


package com.user.provisioning.repository;

import com.user.provisioning.entity.DynamicGroupRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicGroupRuleRepository extends JpaRepository<DynamicGroupRule, Long> {
}


package com.user.provisioning.service;

import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.DynamicGroupRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DynamicGroupRuleService {

    private final DynamicGroupRuleRepository dynamicGroupRuleRepository;

    public List<DynamicGroupRule> getRulesByGroupId(String groupId) {
        Optional<List<DynamicGroupRule>> optionalDynamicGroupRulesules = dynamicGroupRuleRepository.findByGroupId(groupId);
        if(optionalDynamicGroupRulesules.isPresent() && !optionalDynamicGroupRulesules.get().isEmpty()) {
            return optionalDynamicGroupRulesules.get();
        }
        throw new UserProvisioningCustomException(ErrorCode.DYNAMIC_GROUP_RULES_NOT_FOUND);
    }

    public DynamicGroupRule createRule(DynamicGroupRule rule) {
        Optional<DynamicGroupRule> optionalDynamicGroupRule = dynamicGroupRuleRepository.findOneByGroupIdAndAttributeAndOperationAndValue(rule.getGroupId(),
                rule.getAttribute(), rule.getOperation(), rule.getValue());
        if(optionalDynamicGroupRule.isPresent()) {
            throw new UserProvisioningCustomException(ErrorCode.DYNAMIC_GROUP_RULE_ALREADY_EXISTS);
        }
        return dynamicGroupRuleRepository.save(rule);
    }

    public DynamicGroupRule updateRule(String id, DynamicGroupRule ruleDetails) {
        DynamicGroupRule dynamicGroupRule = dynamicGroupRuleRepository.findById(id).orElse(null);
        if (nonNull(dynamicGroupRule)) {
            dynamicGroupRule.setGroupId(ruleDetails.getGroupId());
            dynamicGroupRule.setAttribute(ruleDetails.getAttribute());
            dynamicGroupRule.setOperation(ruleDetails.getOperation());
            dynamicGroupRule.setValue(ruleDetails.getValue());
            return dynamicGroupRuleRepository.save(dynamicGroupRule);
        } else {
            throw new UserProvisioningCustomException(ErrorCode.DYNAMIC_GROUP_RULE_NOT_FOUND);
        }
    }

    public void deleteRule(String id) {
        dynamicGroupRuleRepository.deleteById(id);
    }
}


package com.user.provisioning.controller;

import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.service.DynamicGroupRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dynamic-group-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DynamicGroupRuleController {

    private final DynamicGroupRuleService dynamicGroupRuleService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<DynamicGroupRule>> getRulesByGroupId(@PathVariable String groupId) {
        return ResponseEntity.ok(dynamicGroupRuleService.getRulesByGroupId(groupId));
    }

    @PostMapping
    public ResponseEntity<DynamicGroupRule> createRule(@RequestBody DynamicGroupRule rule) {
        return ResponseEntity.ok(dynamicGroupRuleService.createRule(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DynamicGroupRule> updateRule(@PathVariable String id, @RequestBody DynamicGroupRule ruleDetails) {
        return ResponseEntity.ok(dynamicGroupRuleService.updateRule(id, ruleDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        dynamicGroupRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}


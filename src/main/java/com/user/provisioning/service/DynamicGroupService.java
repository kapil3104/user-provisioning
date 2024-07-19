package com.user.provisioning.service;


import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.entity.MembershipRequest;
import com.user.provisioning.repository.DynamicGroupRuleRepository;
import com.user.provisioning.repository.EmployeeRepository;
import com.user.provisioning.repository.GroupRepository;
import com.user.provisioning.repository.MembershipRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicGroupService {

    private final DynamicGroupRuleRepository dynamicGroupRuleRepository;
    private final EmployeeRepository employeeRepository;
    private final GroupRepository groupRepository;
    private final MembershipRequestRepository membershipRequestRepository;

    public void evaluateAndUpdateDynamicGroups() {

        List<Group> dynamicGroups = groupRepository.findByType("Dynamic");

        for (Group group : dynamicGroups) {
            Optional<List<DynamicGroupRule>> rules = dynamicGroupRuleRepository.findByGroupId(group.getId());

            if(rules.isPresent()) {
                List<Employee> matchingEmployees = employeeRepository.findAll().stream()
                        .filter(employee -> matchesRules(employee, rules.get()))
                        .collect(Collectors.toList());

                updateGroupMembership(group, matchingEmployees);
            }
        }
    }

    private boolean matchesRules(Employee employee, List<DynamicGroupRule> rules) {
        for (DynamicGroupRule rule : rules) {
            if (!matchesRule(employee, rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesRule(Employee employee, DynamicGroupRule rule) {
        String employeeValue = getEmployeeAttributeValue(employee, rule.getAttribute());
        if (employeeValue == null) {
            return false;
        }

        return switch (rule.getOperation().toLowerCase()) {
            case "equals" -> employeeValue.equals(rule.getValue());
            case "contains" -> employeeValue.contains(rule.getValue());
            case "starts_with" -> employeeValue.startsWith(rule.getValue());
            case "ends_with" -> employeeValue.endsWith(rule.getValue());
            default -> false;
        };
    }

    private String getEmployeeAttributeValue(Employee employee, String attribute) {
        return switch (attribute.toLowerCase()) {
            case "role" -> employee.getRole();
            case "location" -> employee.getLocation();
            case "department" -> employee.getDepartment();
            // Add more attributes as needed
            default -> null;
        };
    }

    private void updateGroupMembership(Group group, List<Employee> employees) {
        List<String> employeeIds = employees.stream().map(Employee::getId).toList();
        Optional<List<MembershipRequest>> optionalMembershipRequests = membershipRequestRepository.findByGroupIdAndStatus(group.getId(), "Approved");
        if(optionalMembershipRequests.isPresent()) {
            List<String> currentMemberIds = optionalMembershipRequests.get()
                .stream()
                    .map(MembershipRequest::getEmployeeId)
                    .toList();

            // Add new members
            for (String employeeId : employeeIds) {
                if (!currentMemberIds.contains(employeeId)) {
                    MembershipRequest request = new MembershipRequest();
                    request.setEmployeeId(employeeId);
                    request.setGroupId(group.getId());
                    request.setStatus("Approved");
                    membershipRequestRepository.save(request);
                }
            }

            // Remove members who no longer match the rules
            for (String currentMemberId : currentMemberIds) {
                if (!employeeIds.contains(currentMemberId)) {
                    Optional<MembershipRequest> request = membershipRequestRepository.findByEmployeeIdAndGroupId(currentMemberId, group.getId());
                    request.ifPresent(membershipRequestRepository::delete);
                }
            }
        }
    }
}

package com.user.provisioning.service;

import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.entity.Membership;
import com.user.provisioning.enums.ApprovalStatus;
import com.user.provisioning.repository.EmployeeRepository;
import com.user.provisioning.repository.GroupRepository;
import com.user.provisioning.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing dynamic groups and their memberships.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicGroupService {

    private final EmployeeRepository employeeRepository;
    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;

    /**
     * Evaluates and updates the membership of a dynamic group based on the group's rules.
     * This method is executed asynchronously.
     *
     * @param groupId the ID of the group to evaluate and update
     */
    @Transactional
    @Async("threadPoolTaskExecutor")
    public void evaluateAndUpdateDynamicGroups(Long groupId) {
        log.info("Evaluating and updating dynamic groups for groupId: {}", groupId);
        Optional<Group> dynamicGroupOpt = groupRepository.findById(groupId);

        if (dynamicGroupOpt.isPresent()) {
            Group dynamicGroup = dynamicGroupOpt.get();
            List<DynamicGroupRule> rules = dynamicGroup.getDynamicGroupRules();
            List<Employee> matchingEmployees = employeeRepository.findAll().stream()
                    .filter(employee -> matchesRules(employee, rules))
                    .collect(Collectors.toList());

            updateGroupMembership(dynamicGroup, matchingEmployees);
            log.info("Updated dynamic group {} with matching employees", dynamicGroup.getId());
        } else {
            log.warn("Group with id {} not found", groupId);
        }
    }

    /**
     * Evaluates and assigns dynamic groups for an employee based on the group's rules.
     * This method is executed asynchronously.
     *
     * @param employee the employee to evaluate and assign dynamic groups
     */
    @Transactional
    @Async("threadPoolTaskExecutor")
    public void evaluateAndAssignDynamicGroupForEmployee(Employee employee) {
        log.info("Evaluating and assigning dynamic groups for employeeId: {}", employee.getId());
        List<Group> dynamicGroups = groupRepository.findByType("Dynamic");
        List<Membership> membershipList = new ArrayList<>();

        for (Group dynamicGroup : dynamicGroups) {
            List<DynamicGroupRule> rules = dynamicGroup.getDynamicGroupRules();

            if (matchesRules(employee, rules)) {
                Membership membership = new Membership();
                membership.setEmployee(employee);
                membership.setGroup(dynamicGroup);
                membership.setStatus(ApprovalStatus.APPROVED.name());
                membershipList.add(membership);
                log.info("Employee {} matches dynamic group {}", employee.getId(), dynamicGroup.getId());
            }
        }
        if (!membershipList.isEmpty()) {
            membershipRepository.saveAll(membershipList);
            log.info("Assigned employee {} to {} dynamic groups", employee.getId(), membershipList.size());
        } else {
            log.info("No dynamic group matches found for employee {}", employee.getId());
        }
    }

    /**
     * Updates the membership of a group with the given list of employees.
     *
     * @param group the group to update
     * @param employees the list of employees to set as members of the group
     */
    @Transactional
    protected void updateGroupMembership(Group group, List<Employee> employees) {
        log.info("Updating group membership for groupId: {}", group.getId());
        List<Long> employeeIds = employees.stream().map(Employee::getId).collect(Collectors.toList());
        List<Membership> currentMemberships = membershipRepository.findByGroupIdAndStatus(group.getId(), ApprovalStatus.APPROVED.name()).orElse(Collections.emptyList());

        Set<Long> currentMemberIds = currentMemberships.stream()
                .map(Membership::getEmployee)
                .map(Employee::getId)
                .collect(Collectors.toSet());

        // Members to remove
        employeeIds.forEach(currentMemberIds::remove);

        // Add new members
        List<Membership> membershipsToAdd = employees.stream()
                .map(employee -> {
                    Membership request = new Membership();
                    request.setEmployee(employee);
                    request.setGroup(group);
                    request.setStatus(ApprovalStatus.APPROVED.name());
                    return request;
                })
                .collect(Collectors.toList());

        if (!membershipsToAdd.isEmpty()) {
            membershipRepository.saveAll(membershipsToAdd);
            log.info("Added {} memberships to group {}", membershipsToAdd.size(), group.getId());
        }

        // Remove members who no longer match the rules
        List<Membership> membershipsToDelete = currentMemberships.stream()
                .filter(request -> currentMemberIds.contains(request.getEmployee().getId()))
                .collect(Collectors.toList());
        if (!membershipsToDelete.isEmpty()) {
            membershipRepository.deleteAll(membershipsToDelete);
            log.info("Removed {} memberships from group {}", membershipsToDelete.size(), group.getId());
        }
    }

    /**
     * Checks if an employee matches all the given dynamic group rules.
     *
     * @param employee the employee to check
     * @param rules the list of rules to match against
     * @return true if the employee matches all rules, false otherwise
     */
    private boolean matchesRules(Employee employee, List<DynamicGroupRule> rules) {
        return rules.stream().allMatch(rule -> matchesRule(employee, rule));
    }

    /**
     * Checks if an employee matches a single dynamic group rule.
     *
     * @param employee the employee to check
     * @param rule the rule to match against
     * @return true if the employee matches the rule, false otherwise
     */
    private boolean matchesRule(Employee employee, DynamicGroupRule rule) {
        String employeeValue = getEmployeeAttributeValue(employee, rule.getAttribute());
        if (employeeValue == null) {
            log.debug("Employee {} does not have attribute {}", employee.getId(), rule.getAttribute());
            return false;
        }

        boolean matches = switch (rule.getOperation().toLowerCase()) {
            case "equals" -> employeeValue.equals(rule.getValue());
            case "contains" -> employeeValue.contains(rule.getValue());
            case "starts_with" -> employeeValue.startsWith(rule.getValue());
            case "ends_with" -> employeeValue.endsWith(rule.getValue());
            default -> false;
        };

        if (matches) {
            log.debug("Employee {} matches rule {} with operation {}", employee.getId(), rule.getAttribute(), rule.getOperation());
        } else {
            log.debug("Employee {} does not match rule {} with operation {}", employee.getId(), rule.getAttribute(), rule.getOperation());
        }

        return matches;
    }

    /**
     * Retrieves the value of an employee's attribute based on the given attribute name.
     *
     * @param employee the employee whose attribute value is to be retrieved
     * @param attribute the name of the attribute
     * @return the value of the attribute, or null if the attribute is not found
     */
    private String getEmployeeAttributeValue(Employee employee, String attribute) {
        return switch (attribute.toLowerCase()) {
            case "location" -> employee.getLocation();
            case "department" -> employee.getDepartment();
            default -> null;
        };
    }
}

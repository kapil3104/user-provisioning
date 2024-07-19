package com.user.provisioning.service;

import com.user.provisioning.dto.GroupRequest;
import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.entity.MembershipRequest;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.DynamicGroupRuleRepository;
import com.user.provisioning.repository.EmployeeRepository;
import com.user.provisioning.repository.GroupRepository;
import com.user.provisioning.repository.MembershipRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class GroupService {

    public static final String DYNAMIC = "Dynamic";

    private final GroupRepository groupRepository;
    private final EmployeeService employeeService;
    private final MembershipRequestRepository membershipRequestRepository;
    private final DynamicGroupRuleRepository dynamicGroupRuleRepository;

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public Group getGroupById(String id) {
        Optional<Group> optionalGroup = groupRepository.findById(id);
        if(optionalGroup.isPresent()) {
            return optionalGroup.get();
        }
        throw new UserProvisioningCustomException(ErrorCode.GROUP_NOT_FOUND);
    }

    public Group createGroup(GroupRequest groupRequest) {
        if (DYNAMIC.equalsIgnoreCase(groupRequest.getType()) && (groupRequest.getDynamicGroupRules() == null || groupRequest.getDynamicGroupRules().isEmpty())) {
            throw new UserProvisioningCustomException(ErrorCode.DYNAMIC_GROUP_RULE_VALIDATION_ERROR);
        }
        Group group = new Group();
        group.setName(groupRequest.getName());
        group.setType(groupRequest.getType());
        group.setDescription(groupRequest.getDescription());

        Group savedGroup = groupRepository.save(group);

        if (DYNAMIC.equalsIgnoreCase(groupRequest.getType())) {
            for (DynamicGroupRule rule : groupRequest.getDynamicGroupRules()) {
                rule.setGroupId(savedGroup.getId());
                dynamicGroupRuleRepository.save(rule);
            }
        }

        return savedGroup;
    }

    public Group updateGroup(String id, Group groupDetails) {
        Group group = getGroupById(id);
        if(nonNull(groupDetails.getName()) && !groupDetails.getName().isEmpty()) {
            group.setName(groupDetails.getName());
        }
        if(nonNull(groupDetails.getType()) && !groupDetails.getType().isEmpty()) {
            group.setType(groupDetails.getType());
        }
        if(nonNull(groupDetails.getDescription()) && !groupDetails.getDescription().isEmpty()) {
            group.setDescription(groupDetails.getDescription());
        }
        return groupRepository.save(group);
    }

    public void deleteGroup(String id) {
        groupRepository.deleteById(id);
    }

    public List<Employee> getEmployeesByGroupId(String groupId) {
        Optional<List<MembershipRequest>> optionalMembershipRequest = membershipRequestRepository.findByGroupIdAndStatus(groupId, "Approved");
        if(optionalMembershipRequest.isEmpty()) {
            throw new UserProvisioningCustomException(ErrorCode.EMPLOYEES_NOT_FOUND_FOR_GIVEN_GROUP);
        }
        List<String> approvedMemberIds = optionalMembershipRequest.get()
                .stream()
                .map(MembershipRequest::getEmployeeId)
                .toList();

        return approvedMemberIds.stream()
                .map(this::getEmployeeByIdSafe)
                .filter(Objects::nonNull)
                .toList();
    }

    private Employee getEmployeeByIdSafe(String id) {
        try {
            return employeeService.getEmployeeById(id);
        } catch (UserProvisioningCustomException e) {
            return null;
        }
    }

}


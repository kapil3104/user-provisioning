package com.user.provisioning.service;

import com.user.provisioning.dto.GroupRequest;
import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.entity.Group;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing group-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    public static final String DYNAMIC = "Dynamic";

    private final GroupRepository groupRepository;
    private final DynamicGroupService dynamicGroupService;

    /**
     * Retrieves all groups.
     *
     * @return a list of all groups
     */
    public List<Group> getAllGroups() {
        log.info("Fetching all groups");
        return groupRepository.findAll();
    }

    /**
     * Retrieves a group by its ID.
     *
     * @param id the ID of the group
     * @return the group with the specified ID
     * @throws UserProvisioningCustomException if the group is not found
     */
    public Group getGroupById(Long id) {
        log.info("Fetching group with id: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Group with id {} not found", id);
                    return new UserProvisioningCustomException(ErrorCode.GROUP_NOT_FOUND);
                });
    }

    /**
     * Creates a new group.
     *
     * @param groupRequest the request object containing group details
     * @return the created group
     * @throws UserProvisioningCustomException if there are validation errors in the group request
     */
    public Group createGroup(GroupRequest groupRequest) {
        log.info("Creating group with name: {}", groupRequest.getName());
        validateGroupRequest(groupRequest);

        Group group = new Group();
        group.setName(groupRequest.getName());
        group.setType(groupRequest.getType());
        group.setDescription(groupRequest.getDescription());

        if (isDynamicGroup(groupRequest.getType())) {
            for (DynamicGroupRule rule : groupRequest.getDynamicGroupRules()) {
                rule.setGroup(group);
                group.getDynamicGroupRules().add(rule);
            }
        }
        Group savedGroup = groupRepository.save(group);
        log.info("Group with id {} created successfully", savedGroup.getId());

        if (isDynamicGroup(groupRequest.getType())) {
            dynamicGroupService.evaluateAndUpdateDynamicGroups(savedGroup.getId());
        }
        return savedGroup;
    }

    /**
     * Updates an existing group.
     *
     * @param id           the ID of the group to update
     * @param groupRequest the request object containing updated group details
     * @return the updated group
     * @throws UserProvisioningCustomException if there are validation errors in the group request or the group is not found
     */
    @Transactional
    public Group updateGroup(Long id, GroupRequest groupRequest) {
        log.info("Updating group with id: {}", id);
        validateGroupRequest(groupRequest);

        Group updatedGroup = updateGroupDetails(id, groupRequest);

        if (isDynamicGroup(groupRequest.getType())) {
            updatedGroup.getDynamicGroupRules().clear();
            for (DynamicGroupRule rule : groupRequest.getDynamicGroupRules()) {
                rule.setGroup(updatedGroup);
                updatedGroup.getDynamicGroupRules().add(rule);
            }
        }

        if (isDynamicGroup(groupRequest.getType())) {
            dynamicGroupService.evaluateAndUpdateDynamicGroups(updatedGroup.getId());
        }
        Group savedGroup = groupRepository.save(updatedGroup);
        log.info("Group with id {} updated successfully", savedGroup.getId());
        return savedGroup;
    }

    /**
     * Deletes a group by its ID.
     *
     * @param id the ID of the group to delete
     * @throws UserProvisioningCustomException if the group is not found
     */
    @Transactional
    public void deleteGroup(Long id) {
        log.info("Deleting group with id: {}", id);
        if (groupRepository.existsById(id)) {
            groupRepository.deleteById(id);
            log.info("Group with id {} deleted successfully", id);
        } else {
            log.error("Group with id {} not found", id);
            throw new UserProvisioningCustomException(ErrorCode.GROUP_NOT_FOUND);
        }
    }

    /**
     * Validates the group request.
     *
     * @param groupRequest the request object to validate
     * @throws UserProvisioningCustomException if the validation fails
     */
    private void validateGroupRequest(GroupRequest groupRequest) {
        if (isDynamicGroup(groupRequest.getType()) && (groupRequest.getDynamicGroupRules() == null || groupRequest.getDynamicGroupRules().isEmpty())) {
            log.error("Validation error: Dynamic group rules are missing or empty for group request {}", groupRequest);
            throw new UserProvisioningCustomException(ErrorCode.DYNAMIC_GROUP_RULE_VALIDATION_ERROR);
        }
    }

    /**
     * Checks if the group is of type dynamic.
     *
     * @param type the type of the group
     * @return true if the group is dynamic, false otherwise
     */
    private boolean isDynamicGroup(String type) {
        return DYNAMIC.equalsIgnoreCase(type);
    }

    /**
     * Updates the details of a group.
     *
     * @param id           the ID of the group to update
     * @param groupRequest the request object containing updated group details
     * @return the updated group
     * @throws UserProvisioningCustomException if the group is not found
     */
    private Group updateGroupDetails(Long id, GroupRequest groupRequest) {
        Group group = getGroupById(id);
        if (!groupRequest.getName().isEmpty()) {
            group.setName(groupRequest.getName());
        }
        if (!groupRequest.getType().isEmpty()) {
            group.setType(groupRequest.getType());
        }
        if (groupRequest.getDescription() != null && !groupRequest.getDescription().isEmpty()) {
            group.setDescription(groupRequest.getDescription());
        }
        return group;
    }
}

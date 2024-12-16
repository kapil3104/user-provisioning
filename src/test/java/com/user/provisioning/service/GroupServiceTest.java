package com.user.provisioning.service;

import com.user.provisioning.dto.GroupRequest;
import com.user.provisioning.entity.DynamicGroupRule;
import com.user.provisioning.entity.Group;
import com.user.provisioning.exception.ErrorCode;
import com.user.provisioning.exception.UserProvisioningCustomException;
import com.user.provisioning.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private DynamicGroupService dynamicGroupService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllGroups() {
        List<Group> groups = List.of(new Group(), new Group());
        when(groupRepository.findAll()).thenReturn(groups);

        List<Group> result = groupService.getAllGroups();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    public void testGetGroupById_Success() {
        Group group = new Group();
        group.setId(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        Group result = groupService.getGroupById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(groupRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetGroupById_NotFound() {
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            groupService.getGroupById(1L);
        });

        assertEquals(ErrorCode.GROUP_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    public void testCreateGroup_Success() {
        GroupRequest groupRequest = new GroupRequest("Test Group", "Static");
        groupRequest.setDescription("Test Description");

        Group group = new Group();
        group.setName(groupRequest.getName());
        group.setType(groupRequest.getType());
        group.setDescription(groupRequest.getDescription());

        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group result = groupService.createGroup(groupRequest);

        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(dynamicGroupService, times(0)).evaluateAndUpdateDynamicGroups(any(Long.class));
    }

    @Test
    public void testCreateGroup_DynamicGroup_Success() {
        GroupRequest groupRequest = new GroupRequest("Test Group", "Dynamic");
        groupRequest.setDescription("Test Description");
        groupRequest.setDynamicGroupRules(List.of(new DynamicGroupRule()));

        Group group = new Group();
        group.setId(1L);
        group.setName(groupRequest.getName());
        group.setType(groupRequest.getType());
        group.setDescription(groupRequest.getDescription());

        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group result = groupService.createGroup(groupRequest);

        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(dynamicGroupService, times(1)).evaluateAndUpdateDynamicGroups(group.getId());
    }

    @Test
    public void testCreateGroup_DynamicGroup_ValidationError() {
        GroupRequest groupRequest = new GroupRequest("Test Group", "Dynamic");
        groupRequest.setDescription("Test Description");

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            groupService.createGroup(groupRequest);
        });

        assertEquals(ErrorCode.DYNAMIC_GROUP_RULE_VALIDATION_ERROR, exception.getErrorCode());
        verify(groupRepository, times(0)).save(any(Group.class));
        verify(dynamicGroupService, times(0)).evaluateAndUpdateDynamicGroups(any(Long.class));
    }

    @Test
    public void testUpdateGroup_Success() {
        GroupRequest groupRequest = new GroupRequest("Updated Group", "Static");
        groupRequest.setDescription("Updated Description");

        Group group = new Group();
        group.setId(1L);
        group.setName("Original Group");
        group.setType("Static");
        group.setDescription("Original Description");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group result = groupService.updateGroup(1L, groupRequest);

        assertNotNull(result);
        assertEquals("Updated Group", result.getName());
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(dynamicGroupService, times(0)).evaluateAndUpdateDynamicGroups(any(Long.class));
    }

    @Test
    public void testUpdateGroup_DynamicGroup_Success() {
        GroupRequest groupRequest = new GroupRequest("Updated Group", "Dynamic");
        groupRequest.setDescription("Updated Description");
        groupRequest.setDynamicGroupRules(List.of(new DynamicGroupRule()));

        Group group = new Group();
        group.setId(1L);
        group.setName("Original Group");
        group.setType("Dynamic");
        group.setDescription("Original Description");

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenReturn(group);

        Group result = groupService.updateGroup(1L, groupRequest);

        assertNotNull(result);
        assertEquals("Updated Group", result.getName());
        verify(groupRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(dynamicGroupService, times(1)).evaluateAndUpdateDynamicGroups(group.getId());
    }

    @Test
    public void testDeleteGroup_Success() {
        when(groupRepository.existsById(1L)).thenReturn(true);

        groupService.deleteGroup(1L);

        verify(groupRepository, times(1)).existsById(1L);
        verify(groupRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteGroup_NotFound() {
        when(groupRepository.existsById(1L)).thenReturn(false);

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            groupService.deleteGroup(1L);
        });

        assertEquals(ErrorCode.GROUP_NOT_FOUND, exception.getErrorCode());
        verify(groupRepository, times(1)).existsById(1L);
    }

    @Test
    public void testValidateGroupRequest_DynamicGroupWithRules() {
        GroupRequest groupRequest = new GroupRequest("Test Group", "Dynamic");
        groupRequest.setDynamicGroupRules(List.of(new DynamicGroupRule()));

        Group group = new Group();
        group.setName(groupRequest.getName());
        group.setType(groupRequest.getType());
        group.setDescription(groupRequest.getDescription());

        when(groupRepository.save(any(Group.class))).thenReturn(group);

        assertDoesNotThrow(() -> groupService.createGroup(groupRequest));
    }

    @Test
    public void testValidateGroupRequest_DynamicGroupWithoutRules() {
        GroupRequest groupRequest = new GroupRequest("Test Group", "Dynamic");

        UserProvisioningCustomException exception = assertThrows(UserProvisioningCustomException.class, () -> {
            groupService.createGroup(groupRequest);
        });

        assertEquals(ErrorCode.DYNAMIC_GROUP_RULE_VALIDATION_ERROR, exception.getErrorCode());
    }
}

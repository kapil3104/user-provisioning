package com.user.provisioning.service;

import com.user.provisioning.entity.*;
import com.user.provisioning.enums.ApprovalStatus;
import com.user.provisioning.enums.ERole;
import com.user.provisioning.repository.EmployeeRepository;
import com.user.provisioning.repository.GroupRepository;
import com.user.provisioning.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class DynamicGroupServiceTest {

    @InjectMocks
    private DynamicGroupService dynamicGroupService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testEvaluateAndUpdateDynamicGroups_Positive() {
        Long groupId = 1L;
        Group group = new Group();
        group.setId(groupId);
        group.setType("Dynamic");
        List<DynamicGroupRule> rules = new ArrayList<>();
        group.setDynamicGroupRules(rules);

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setLocation("NY");
        employee.setDepartment("IT");

        List<Employee> employees = List.of(employee);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(employeeRepository.findAll()).thenReturn(employees);

        dynamicGroupService.evaluateAndUpdateDynamicGroups(groupId);

        verify(membershipRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testEvaluateAndUpdateDynamicGroups_GroupNotFound() {
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        dynamicGroupService.evaluateAndUpdateDynamicGroups(groupId);

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    public void testEvaluateAndAssignDynamicGroupForEmployee_Positive() {

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setLocation("NY");
        employee.setDepartment("IT");

        Group group = new Group();
        group.setId(1L);
        group.setType("Dynamic");
        List<DynamicGroupRule> rules = new ArrayList<>();
        group.setDynamicGroupRules(rules);

        List<Group> groups = List.of(group);

        when(groupRepository.findByType("Dynamic")).thenReturn(groups);

        dynamicGroupService.evaluateAndAssignDynamicGroupForEmployee(employee);

        verify(membershipRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testEvaluateAndAssignDynamicGroupForEmployee_NoDynamicGroups() {
        Employee employee = new Employee();
        when(groupRepository.findByType("Dynamic")).thenReturn(Collections.emptyList());

        dynamicGroupService.evaluateAndAssignDynamicGroupForEmployee(employee);

        verify(membershipRepository, never()).saveAll(anyList());
    }

    @Test
    public void testUpdateGroupMembership_Positive() {
        Group group = new Group();
        group.setId(1L);

        Employee employee1 = new Employee();
        employee1.setId(1L);
        Employee employee2 = new Employee();
        employee2.setId(2L);

        List<Employee> employees = List.of(employee1, employee2);

        Membership membership1 = new Membership();
        membership1.setEmployee(employee1);
        membership1.setGroup(group);
        membership1.setStatus(ApprovalStatus.APPROVED.name());

        List<Membership> memberships = List.of(membership1);

        when(membershipRepository.findByGroupIdAndStatus(group.getId(), ApprovalStatus.APPROVED.name())).thenReturn(Optional.of(memberships));

        dynamicGroupService.updateGroupMembership(group, employees);

        verify(membershipRepository, times(1)).saveAll(anyList());
//        verify(membershipRepository, times(1)).deleteAll(anyList());
    }

    @Test
    public void testUpdateGroupMembership_NoCurrentMembers() {
        Group group = new Group();
        group.setId(1L);

        Employee employee1 = new Employee();
        employee1.setId(1L);
        Employee employee2 = new Employee();
        employee2.setId(2L);

        List<Employee> employees = List.of(employee1, employee2);

        when(membershipRepository.findByGroupIdAndStatus(group.getId(), ApprovalStatus.APPROVED.name())).thenReturn(Optional.empty());

        dynamicGroupService.updateGroupMembership(group, employees);

        verify(membershipRepository, times(1)).saveAll(anyList());
        verify(membershipRepository, never()).deleteAll(anyList());
    }

    @Test
    public void testUpdateGroupMembership_EmptyEmployees() {
        Group group = new Group();
        group.setId(1L);

        List<Employee> employees = Collections.emptyList();

        Membership membership1 = new Membership();
        membership1.setEmployee(new Employee());
        membership1.setGroup(group);
        membership1.setStatus(ApprovalStatus.APPROVED.name());

        List<Membership> memberships = List.of(membership1);

        when(membershipRepository.findByGroupIdAndStatus(group.getId(), ApprovalStatus.APPROVED.name())).thenReturn(Optional.of(memberships));

        dynamicGroupService.updateGroupMembership(group, employees);

        verify(membershipRepository, never()).saveAll(anyList());
//        verify(membershipRepository, times(1)).deleteAll(anyList());
    }
}

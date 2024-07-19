package com.user.provisioning.controller;

import com.user.provisioning.dto.GroupRequest;
import com.user.provisioning.entity.Employee;
import com.user.provisioning.entity.Group;
import com.user.provisioning.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Group>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Group> getGroupById(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getGroupById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> createGroup(@RequestBody GroupRequest groupRequest) {
        return ResponseEntity.ok(groupService.createGroup(groupRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> updateGroup(@PathVariable String id, @RequestBody Group groupDetails) {
        return ResponseEntity.ok(groupService.updateGroup(id, groupDetails));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Employee>> getEmployeesByGroupId(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getEmployeesByGroupId(id));
    }
}

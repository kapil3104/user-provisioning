package com.user.provisioning.controller;

import com.user.provisioning.dto.GroupRequest;
import com.user.provisioning.dto.MessageResponse;
import com.user.provisioning.entity.Group;
import com.user.provisioning.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<Group>> getAllGroups() {
        log.info("Fetching all groups");
        List<Group> groups = groupService.getAllGroups();
        log.info("Fetched {} groups", groups.size());
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        log.info("Fetching group with id: {}", id);
        Group group = groupService.getGroupById(id);
        log.info("Fetched group: {}", group);
        return ResponseEntity.ok(group);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> createGroup(@RequestBody GroupRequest groupRequest) {
        log.info("Creating new group: {}", groupRequest);
        Group createdGroup = groupService.createGroup(groupRequest);
        log.info("Created group: {}", createdGroup);
        return ResponseEntity.ok(createdGroup);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody GroupRequest groupDetails) {
        log.info("Updating group with id: {}", id);
        Group updatedGroup = groupService.updateGroup(id, groupDetails);
        log.info("Updated group: {}", updatedGroup);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteGroup(@PathVariable Long id) {
        log.info("Deleting group with id: {}", id);
        groupService.deleteGroup(id);
        log.info("Deleted group with id: {}", id);
        return ResponseEntity.ok(new MessageResponse("Group record has been deleted successfully"));
    }
}

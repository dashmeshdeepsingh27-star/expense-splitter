package com.expense_splitter.expense_splitter.controller;

import com.expense_splitter.expense_splitter.dto.CreateGroupRequest;
import com.expense_splitter.expense_splitter.model.Group;
import com.expense_splitter.expense_splitter.model.User;
import com.expense_splitter.expense_splitter.repository.GroupRepository;
import com.expense_splitter.expense_splitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(loggedInEmail);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User currentUser = userOptional.get();

        Group group = new Group();
        group.setName(request.getName());
        group.setCreatedBy(currentUser);

        HashSet<User> members = new HashSet<>();
        members.add(currentUser);
        group.setMembers(members);

        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }


    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @RequestBody com.expense_splitter.expense_splitter.dto.AddMemberRequest request) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOptional.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        boolean isMember = group.getMembers().stream()
                .anyMatch(member -> member.getEmail().equals(loggedInEmail));

        if (!isMember) {
            return ResponseEntity.status(403).body("Only group members can add new members");
        }

        Optional<User> userToAddOptional = userRepository.findByEmail(request.getEmail());

        if (userToAddOptional.isEmpty()) {
            return ResponseEntity.status(404).body("User with that email not found");
        }

        User userToAdd = userToAddOptional.get();

        group.getMembers().add(userToAdd);
        groupRepository.save(group);

        return ResponseEntity.ok(group);
    }
}
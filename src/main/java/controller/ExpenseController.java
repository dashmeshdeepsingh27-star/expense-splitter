package com.expense_splitter.expense_splitter.controller;

import com.expense_splitter.expense_splitter.dto.AddExpenseRequest;
import com.expense_splitter.expense_splitter.model.Expense;
import com.expense_splitter.expense_splitter.model.Group;
import com.expense_splitter.expense_splitter.model.User;
import com.expense_splitter.expense_splitter.repository.ExpenseRepository;
import com.expense_splitter.expense_splitter.repository.GroupRepository;
import com.expense_splitter.expense_splitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addExpense(@PathVariable Long groupId, @RequestBody AddExpenseRequest request) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOptional.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        Optional<User> userOptional = userRepository.findByEmail(loggedInEmail);
        User currentUser = userOptional.get();

        boolean isMember = group.getMembers().stream()
                .anyMatch(member -> member.getEmail().equals(loggedInEmail));

        if (!isMember) {
            return ResponseEntity.status(403).body("Only group members can add expenses");
        }

        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setGroup(group);
        expense.setPaidBy(currentUser);

        expenseRepository.save(expense);

        return ResponseEntity.ok(expense);
    }

    @GetMapping
    public ResponseEntity<?> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return ResponseEntity.ok(expenses);
    }
}
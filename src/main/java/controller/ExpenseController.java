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
import com.expense_splitter.expense_splitter.dto.BalanceResponse;
import com.expense_splitter.expense_splitter.model.ExpenseShare;
import com.expense_splitter.expense_splitter.repository.ExpenseShareRepository;
import com.expense_splitter.expense_splitter.model.Payment;
import com.expense_splitter.expense_splitter.repository.PaymentRepository;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseShareRepository expenseShareRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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

        // Validate custom shares if provided
        if (request.getShares() != null && !request.getShares().isEmpty()) {
            double totalShares = request.getShares().stream()
                    .mapToDouble(AddExpenseRequest.ShareRequest::getAmount)
                    .sum();

            if (Math.round(totalShares * 100.0) / 100.0 != Math.round(request.getAmount() * 100.0) / 100.0) {
                return ResponseEntity.badRequest().body("Shares must add up to the total amount");
            }
        }

        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setGroup(group);
        expense.setPaidBy(currentUser);

        expenseRepository.save(expense);

        // If custom shares were provided, save them
        if (request.getShares() != null && !request.getShares().isEmpty()) {
            for (AddExpenseRequest.ShareRequest shareRequest : request.getShares()) {
                Optional<User> shareUserOptional = userRepository.findByEmail(shareRequest.getEmail());

                if (shareUserOptional.isEmpty()) {
                    return ResponseEntity.badRequest().body("User not found: " + shareRequest.getEmail());
                }

                ExpenseShare share = new ExpenseShare();
                share.setExpense(expense);
                share.setUser(shareUserOptional.get());
                share.setAmount(shareRequest.getAmount());

                expenseShareRepository.save(share);
            }
        }

        return ResponseEntity.ok(expense);
    }
    @GetMapping
    public ResponseEntity<?> getExpenses(@PathVariable Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/settlement")
    public ResponseEntity<?> getSettlement(@PathVariable Long groupId) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);

        if (groupOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOptional.get();
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        Map<Long, Double> balances = new HashMap<>();

        for (User member : group.getMembers()) {
            balances.put(member.getId(), 0.0);
        }

        int memberCount = group.getMembers().size();

        for (Expense expense : expenses) {

            Long payerId = expense.getPaidBy().getId();
            balances.put(payerId, balances.get(payerId) + expense.getAmount());

            List<ExpenseShare> customShares = expenseShareRepository.findByExpenseId(expense.getId());

            if (!customShares.isEmpty()) {
                for (ExpenseShare share : customShares) {
                    Long userId = share.getUser().getId();
                    balances.put(userId, balances.get(userId) - share.getAmount());
                }
            } else {
                double equalShare = expense.getAmount() / memberCount;
                for (User member : group.getMembers()) {
                    balances.put(member.getId(), balances.get(member.getId()) - equalShare);
                }
            }
        }

        // Factor in recorded payments
        List<Payment> payments = paymentRepository.findByGroupId(groupId);
        for (Payment payment : payments) {
            Long payerId = payment.getPaidBy().getId();
            Long receiverId = payment.getPaidTo().getId();

            balances.put(payerId, balances.get(payerId) + payment.getAmount());
            balances.put(receiverId, balances.get(receiverId) - payment.getAmount());
        }

        List<BalanceResponse> result = new ArrayList<>();

        for (User member : group.getMembers()) {
            double balance = Math.round(balances.get(member.getId()) * 100.0) / 100.0;
            result.add(new BalanceResponse(member.getName(), member.getEmail(), balance));
        }

        return ResponseEntity.ok(result);
    }
}
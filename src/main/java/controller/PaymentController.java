package com.expense_splitter.expense_splitter.controller;

import com.expense_splitter.expense_splitter.dto.AddPaymentRequest;
import com.expense_splitter.expense_splitter.model.Group;
import com.expense_splitter.expense_splitter.model.Payment;
import com.expense_splitter.expense_splitter.model.User;
import com.expense_splitter.expense_splitter.repository.GroupRepository;
import com.expense_splitter.expense_splitter.repository.PaymentRepository;
import com.expense_splitter.expense_splitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> addPayment(@PathVariable Long groupId, @RequestBody AddPaymentRequest request) {

        Optional<Group> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            return ResponseEntity.status(404).body("Group not found");
        }

        Group group = groupOptional.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();

        Optional<User> payerOptional = userRepository.findByEmail(loggedInEmail);
        User payer = payerOptional.get();

        Optional<User> receiverOptional = userRepository.findByEmail(request.getPaidToEmail());
        if (receiverOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Recipient not found");
        }

        Payment payment = new Payment();
        payment.setGroup(group);
        payment.setPaidBy(payer);
        payment.setPaidTo(receiverOptional.get());
        payment.setAmount(request.getAmount());

        paymentRepository.save(payment);

        return ResponseEntity.ok(payment);
    }
}
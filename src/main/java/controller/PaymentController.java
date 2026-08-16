package com.expense_splitter.expense_splitter.controller;

import com.expense_splitter.expense_splitter.dto.AddPaymentRequest;
import com.expense_splitter.expense_splitter.dto.PaymentResponse;
import com.expense_splitter.expense_splitter.dto.UserResponse;
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

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                toUserResponse(payment.getPaidBy()),
                toUserResponse(payment.getPaidTo()),
                payment.getAmount(),
                payment.getCreatedAt()
        );
    }

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

        User receiver = receiverOptional.get();

        if(receiver.getId().equals(payer.getId())) {
            return  ResponseEntity.badRequest().body("You cannot record a payment to yourself");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than zero");
        }

        Payment payment = new Payment();
        payment.setGroup(group);
        payment.setPaidBy(payer);
        payment.setPaidTo(receiver);
        payment.setAmount(request.getAmount());

        paymentRepository.save(payment);

        return ResponseEntity.ok(toPaymentResponse(payment));
    }

    @GetMapping
    public ResponseEntity<?> getPayments(@PathVariable Long groupId) {
        var payments = paymentRepository.findByGroupId(groupId).stream()
                .map(this::toPaymentResponse)
                .toList();
        return ResponseEntity.ok(payments);
    }
}
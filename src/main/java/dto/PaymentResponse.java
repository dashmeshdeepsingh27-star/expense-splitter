package com.expense_splitter.expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private com.expense_splitter.expense_splitter.dto.UserResponse paidBy;
    private com.expense_splitter.expense_splitter.dto.UserResponse paidTo;
    private Double amount;
    private LocalDateTime createdAt;
}
package com.expense_splitter.expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private Double amount;
    private String description;
    private UserResponse paidBy;
    private LocalDateTime createdAt;
}
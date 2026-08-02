package com.expense_splitter.expense_splitter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddExpenseRequest {
    private Double amount;
    private String description;
}
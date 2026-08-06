package com.expense_splitter.expense_splitter.dto;

import lombok.*;
@Getter
@Setter
@AllArgsConstructor
public class BalanceResponse {
    private String name;
    private String email;
    private Double balance;
}

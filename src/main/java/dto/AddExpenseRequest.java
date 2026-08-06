package com.expense_splitter.expense_splitter.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
public class AddExpenseRequest {
    private Double amount;
    private String description;

    private List<ShareRequest> shares;

    @Getter
    @Setter
    public static class ShareRequest {
        private String email;
        private Double amount;
    }
}
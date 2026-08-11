package com.expense_splitter.expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private UserResponse createdBy;
    private List<UserResponse> members;
}
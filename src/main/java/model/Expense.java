package com.expense_splitter.expense_splitter.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private com.expense_splitter.expense_splitter.model.Group group;

    @ManyToOne
    @JoinColumn(name = "paid_by", nullable = false)
    private com.expense_splitter.expense_splitter.model.User paidBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
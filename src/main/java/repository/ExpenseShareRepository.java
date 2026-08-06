package com.expense_splitter.expense_splitter.repository;

import com.expense_splitter.expense_splitter.model.ExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseShareRepository extends JpaRepository<ExpenseShare, Long> {

    List<ExpenseShare> findByExpenseId(Long expenseId);

    List<ExpenseShare> findByExpenseGroupId(Long groupId);
}
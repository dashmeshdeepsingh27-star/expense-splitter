package com.expense_splitter.expense_splitter.repository;

import com.expense_splitter.expense_splitter.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByGroupId(Long groupId);
}
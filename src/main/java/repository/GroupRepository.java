package com.expense_splitter.expense_splitter.repository;

import com.expense_splitter.expense_splitter.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    List<Group> findByCreatedByEmail(String email);
}
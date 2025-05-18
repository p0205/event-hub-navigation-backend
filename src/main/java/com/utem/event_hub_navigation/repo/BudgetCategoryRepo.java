package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.BudgetCategory;

@Repository
public interface BudgetCategoryRepo  extends JpaRepository<BudgetCategory, Integer> {

    Page<BudgetCategory> findAll(@NonNull Pageable pageable);
    List<BudgetCategory> findByNameContains(String name);
}

package com.utem.event_hub_navigation.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.utem.event_hub_navigation.model.BudgetCategory;

@Repository
public interface BudgetCategoryRepo  extends JpaRepository<BudgetCategory, Integer> {

    
    List<BudgetCategory> findByNameContains(String name);
}

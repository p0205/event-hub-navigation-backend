package com.utem.event_hub_navigation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.repo.BudgetCategoryRepo;

@Service
public class BudgetCategoryService {

    private BudgetCategoryRepo budgetCategoryRepo;

    @Autowired
    public BudgetCategoryService(BudgetCategoryRepo budgetCategoryRepo) {
        this.budgetCategoryRepo = budgetCategoryRepo;
    }

    public BudgetCategory createBudgetCategory(BudgetCategory budgetCategory) {
        return budgetCategoryRepo.save(budgetCategory);
    }
    public void deleteBudgetCategory(Integer id) {
        budgetCategoryRepo.deleteById(id);
    }

   

}

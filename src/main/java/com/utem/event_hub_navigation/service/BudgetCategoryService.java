package com.utem.event_hub_navigation.service;

import java.util.List;

import com.utem.event_hub_navigation.model.BudgetCategory;

public interface BudgetCategoryService {

    BudgetCategory createBudgetCategory(BudgetCategory budgetCategory);

    void deleteBudgetCategory(Integer id);

    List<BudgetCategory> getAllBudgetCategories();

    BudgetCategory getBudgetCategoryById(Integer id);

    List<BudgetCategory> getBudgetCategoryByNameLike(String name);

}
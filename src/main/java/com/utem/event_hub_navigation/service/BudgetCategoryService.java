package com.utem.event_hub_navigation.service;

import java.util.List;
import java.util.Optional;

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

    public List<BudgetCategory> getAllBudgetCategories(){
        return budgetCategoryRepo.findAll();
    }

    public BudgetCategory getBudgetCategoryById(Integer id) {
        Optional<BudgetCategory> optional = budgetCategoryRepo.findById(id);
        if(optional.isEmpty())
            return null;
        return optional.get();
    }

   

}

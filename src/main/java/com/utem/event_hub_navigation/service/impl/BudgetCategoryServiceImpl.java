package com.utem.event_hub_navigation.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.repo.BudgetCategoryRepo;
import com.utem.event_hub_navigation.service.BudgetCategoryService;

@Service
public class BudgetCategoryServiceImpl implements BudgetCategoryService {

    private BudgetCategoryRepo budgetCategoryRepo;

    @Autowired
    public BudgetCategoryServiceImpl(BudgetCategoryRepo budgetCategoryRepo) {
        this.budgetCategoryRepo = budgetCategoryRepo;
    }

    @Override
    public BudgetCategory createBudgetCategory(BudgetCategory budgetCategory) {
        return budgetCategoryRepo.save(budgetCategory);
    }
    @Override
    public void deleteBudgetCategory(Integer id) {
        budgetCategoryRepo.deleteById(id);
    }

    @Override
    public List<BudgetCategory> getAllBudgetCategories(){
        return budgetCategoryRepo.findAll();
    }

    @Override
    public BudgetCategory getBudgetCategoryById(Integer id) {
        Optional<BudgetCategory> optional = budgetCategoryRepo.findById(id);
        if(optional.isEmpty())
            return null;
        return optional.get();
    }

   

}

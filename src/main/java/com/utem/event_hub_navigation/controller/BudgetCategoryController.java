package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utem.event_hub_navigation.model.BudgetCategory;
import com.utem.event_hub_navigation.service.BudgetCategoryService;

@RestController
@RequestMapping("/budgetCategory")
public class BudgetCategoryController {

    private BudgetCategoryService budgetCategoryService;

    @Autowired
    public BudgetCategoryController(BudgetCategoryService budgetCategoryService) {
        this.budgetCategoryService = budgetCategoryService;
    }

    @PostMapping
    public ResponseEntity<BudgetCategory> createBudgetCategory(@RequestBody BudgetCategory budgetCategory) {
        BudgetCategory createdBudgetCategory = budgetCategoryService.createBudgetCategory(budgetCategory);
        return new ResponseEntity<>(createdBudgetCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetCategory(@PathVariable Integer id) {
        budgetCategoryService.deleteBudgetCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<BudgetCategory>> getAllBudgetCategories() {
        List<BudgetCategory> budgetCategories = budgetCategoryService.getAllBudgetCategories();
        return new ResponseEntity<>(budgetCategories, HttpStatus.OK);
    }

}

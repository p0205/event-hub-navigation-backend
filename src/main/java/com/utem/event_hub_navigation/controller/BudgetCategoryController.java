package com.utem.event_hub_navigation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<BudgetCategory> createBudgetCategory(@RequestBody String name) {
        BudgetCategory request = BudgetCategory.builder().name(name).build();
        BudgetCategory createdBudgetCategory = budgetCategoryService.createBudgetCategory(request);
        return new ResponseEntity<>(createdBudgetCategory, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetCategory(@PathVariable Integer id) {
        budgetCategoryService.deleteBudgetCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetCategory> getBudgetCategoryById(@PathVariable Integer id) {
        BudgetCategory budgetCategory = budgetCategoryService.getBudgetCategoryById(id);
        if (budgetCategory == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(budgetCategory, HttpStatus.OK);
    }

    @GetMapping("/byName")
    public ResponseEntity<List<BudgetCategory>> getBudgetCategoryByName(@RequestParam("name") String name) {
        List<BudgetCategory> budgetCategory = budgetCategoryService.getBudgetCategoryByNameLike(name);
        if (budgetCategory == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(budgetCategory, HttpStatus.OK);
    }

    // @GetMapping
    // public ResponseEntity<List<BudgetCategory>> getAllBudgetCategories() {
    //     List<BudgetCategory> budgetCategories = budgetCategoryService.getAllBudgetCategories();
    //     return new ResponseEntity<>(budgetCategories, HttpStatus.OK);
    // }

    @GetMapping
    public ResponseEntity<?> getAllBudgetCategories(
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name").ascending());
            Page<BudgetCategory> budgetCategories = budgetCategoryService.getAllBudgetCategoriesByPage(pageable);
            return new ResponseEntity<>(budgetCategories, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to remove member: " + e.getMessage());
        }
    }

}

package com.project.shopapp.controller;

import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        this.categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok().body("Create category successfully");
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategory(){
        return ResponseEntity.ok().body(this.categoryService.getAllCategory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> fetchCategoryById(@PathVariable("id") long id){
        return ResponseEntity.ok().body(this.categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        this.categoryService.getCategoryById(id);
        this.categoryService.updateCategory(categoryDTO, id);
        return ResponseEntity.ok("Update category successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") long id){
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok("Delete category success");
    }
}

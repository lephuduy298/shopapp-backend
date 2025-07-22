package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.dto.res.ResCategory;
import com.project.shopapp.models.Category;
import com.project.shopapp.services.CategoryService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final LocalizationUtils localizationUtils;

    @PostMapping
    public ResponseEntity<ResCategory> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        ResCategory resCategory = new ResCategory();

        resCategory.setCategory(this.categoryService.createCategory(categoryDTO));
        resCategory.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY));


        return ResponseEntity.ok().body(resCategory);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategory(){
        return ResponseEntity.ok().body(this.categoryService.getAllCategory());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> getAllBrands(){
        return ResponseEntity.ok().body(this.categoryService.getAllBrand());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> fetchCategoryById(@PathVariable("id") long id){
        return ResponseEntity.ok().body(this.categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {

        ResCategory resCategory = new ResCategory();

        this.categoryService.getCategoryById(id);
        resCategory.setCategory(this.categoryService.updateCategory(categoryDTO, id));
        resCategory.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY));
        return ResponseEntity.ok(resCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable("id") long id){
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY));
    }
}

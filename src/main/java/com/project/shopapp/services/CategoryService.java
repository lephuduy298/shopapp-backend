package com.project.shopapp.services;

import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.services.iservice.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory = Category
                .builder()
                .name(categoryDTO.getName())
                .build();
        return this.categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(long id) {
        return this.categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public List<Category> getAllCategory() {
        return this.categoryRepository.findAll();
    }

    @Transactional
    @Override
    public Category updateCategory(CategoryDTO categoryDTO, long id) {
        Category currentCategory = this.getCategoryById(id);
        currentCategory.setName(categoryDTO.getName());
        return this.categoryRepository.save(currentCategory);
    }

    @Transactional
    @Override
    public void deleteCategory(long id) {
        this.categoryRepository.deleteById(id);
    }

    @Override
    public List<String> getAllBrand() {
        return this.categoryRepository.findAllBrands();
    }
}

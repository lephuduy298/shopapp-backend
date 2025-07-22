package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;

import java.util.List;

public interface ICategoryService {

    Category createCategory(CategoryDTO categoryDTO);
    Category getCategoryById(long id);
    List<Category> getAllCategory();

    Category updateCategory (CategoryDTO categoryDTO, long id);

    void deleteCategory(long id);

    List<String> getAllBrand();
}

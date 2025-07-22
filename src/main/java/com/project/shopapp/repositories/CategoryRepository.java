package com.project.shopapp.repositories;

import com.project.shopapp.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT DISTINCT ps.specValue FROM ProductSpecification ps WHERE ps.specName = 'Hãng sản xuất'")
    List<String> findAllBrands();
}

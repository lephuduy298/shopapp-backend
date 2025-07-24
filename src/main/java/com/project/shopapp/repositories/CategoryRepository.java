package com.project.shopapp.repositories;

import com.project.shopapp.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT DISTINCT ps.specValue FROM ProductSpecification ps WHERE ps.specName = 'Hãng sản xuất'")
    List<String> findAllBrands();

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

}

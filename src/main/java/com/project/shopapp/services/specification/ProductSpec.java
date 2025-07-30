package com.project.shopapp.services.specification;

import com.project.shopapp.models.PriceRange;
import com.project.shopapp.models.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpec {

    public static Specification<Product> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("category").get("name")), pattern)
            );
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null || categoryId == 0) {
                return cb.conjunction();
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> hasBrand(List<String> brands) {
        return (root, query, cb) -> {
            if (brands == null || brands.isEmpty()) {
                return cb.conjunction();
            }

            Join<Product, ProductSpec> specJoin = root.join("specifications", JoinType.LEFT);
            return cb.and(
                    cb.equal(cb.lower(specJoin.get("specName")), "hãng sản xuất"),
                    specJoin.get("specValue").in(brands)
            );
        };
    }

    public static Specification<Product> inPriceRanges(List<PriceRange> ranges) {
        return (root, query, cb) -> {
            if (ranges == null || ranges.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (PriceRange range : ranges) {
                predicates.add(cb.between(root.get("price"), range.getMin(), range.getMax()));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}

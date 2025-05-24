package com.project.shopapp.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Product;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResProduct extends ResponseBase {

    private String name;
    private Float price;
    private String thumbnail;
    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    public static ResProduct convertToResProduct(Product product){
        ResProduct resProduct = ResProduct.builder()
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .build();
        resProduct.setCreatedAt(product.getCreatedAt());
        resProduct.setUpdatedAt(product.getUpdatedAt());
        return resProduct;
    }
}

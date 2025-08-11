package com.project.shopapp.dto.res;

import com.project.shopapp.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResCartItem {
    private Long id;
    private ProductDTO product;
    private Integer quantity;
    // Thêm các trường khác nếu cần
}

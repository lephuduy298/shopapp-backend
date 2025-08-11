package com.project.shopapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResCart {
    private Long id;
    // Đổi List<CartItem> thành List<ResCartItem> để tránh lỗi tuần hoàn khi serialize
    private List<ResCartItem> items;
    private Long userId;
}

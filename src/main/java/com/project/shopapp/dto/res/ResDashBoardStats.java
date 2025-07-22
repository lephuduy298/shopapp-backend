package com.project.shopapp.dto.res;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResDashBoardStats {
    private long totalOrders;
    private long totalProducts;
    private long totalCategories;
    private long totalUsers;

}

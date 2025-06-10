package com.project.shopapp.dto.res;

import com.project.shopapp.models.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResCategory {
    private String message;

    private Category category;
}

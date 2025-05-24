package com.project.shopapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    @NotBlank(message = "Category's name cannot be empty")
    private String name;
}

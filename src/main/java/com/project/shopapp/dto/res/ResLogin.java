package com.project.shopapp.dto.res;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResLogin {
    private String message;

    private String token;
}

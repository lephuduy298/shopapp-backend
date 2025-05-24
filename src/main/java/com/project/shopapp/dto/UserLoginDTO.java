package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginDTO {

    @NotBlank(message = "Phone number is required to login")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}

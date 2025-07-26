package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageDTO {
    @JsonProperty("product_id")
    @Min(value = 1, message = "Product's ID must be > 0")
    private long productId;

    @Size(min = 5, max = 200, message = "Image's name")
    @JsonProperty("image_url")
    private String imageUrl;

//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Getter
//    @Setter
//    public static class UpdateProcductDTO {
//        @JsonProperty("fullname")
//        private String fullName;
//
//        @JsonProperty("phone_number")
//        private String phoneNumber;
//
//        private String address;
//
//        private String password;
//
//        @JsonProperty("retype_password")
//        private String retypePassword;
//
//        @JsonProperty("date_of_birth")
//        private Date dateOfBirth;
//
//        @JsonProperty("facebook_account_id")
//        private int facebookAccountId;
//
//        @JsonProperty("google_account_id")
//        private int googleAccountId;
//    }
}

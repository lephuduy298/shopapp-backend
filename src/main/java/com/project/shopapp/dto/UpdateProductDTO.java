package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private Float price;

    private String description;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("thumbnail")
    private String thumbnail;

    private List<String> urls;
//    private List<MultipartFile> files;
//    @JsonProperty("date_of_birth")
//    private Date dateOfBirth;
//
//    @JsonProperty("facebook_account_id")
//    private int facebookAccountId;
//
//    @JsonProperty("google_account_id")
//    private int googleAccountId;
}


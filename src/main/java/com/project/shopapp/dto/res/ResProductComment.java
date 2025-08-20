package com.project.shopapp.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.ProductComment;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResProductComment {

    private Long id;

    @JsonProperty("content")
    private String content;

    //user's information
    @JsonProperty("user")
    private ResUser resUser;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    public static ResProductComment fromComment(ProductComment productComment) {
        return ResProductComment.builder()
                .id(productComment.getId())
                .content(productComment.getContent())
                .resUser(ResUser.convertToResUser(productComment.getUser()))
                .updatedAt(productComment.getUpdatedAt())
                .build();
    }
}


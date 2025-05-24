package com.project.shopapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultPagination {
    private Object result;

    private Meta meta;

    @Getter
    @Setter
    public static class Meta{
        private long totalPage;
    }
}

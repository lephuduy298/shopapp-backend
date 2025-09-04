package com.project.shopapp.dto.res;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResVNPay {
    private boolean success;
    private  Long orderId;
}

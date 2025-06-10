package com.project.shopapp.dto.res;

import com.project.shopapp.models.User;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResRegister {
    private String message;

    private ResUser resUser;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ResUser extends ResponseBase{
        private String fullName;

        private String phoneNumber;

        private String address;

        private String password;

        private boolean active;

        private Date dateOfBirth;

        private int facebookAccountId;

        private int googleAccountId;

        private String role;

        public static ResRegister.ResUser convertToResUser(User user){
            ResRegister.ResUser resUser = ResRegister.ResUser.builder()
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .address(user.getAddress())
                    .active(user.isActive())
                    .dateOfBirth(user.getDateOfBirth())
                    .facebookAccountId(user.getFacebookAccountId())
                    .googleAccountId(user.getGoogleAccountId())
                    .role(user.getRole().getName())
                    .build();
            resUser.setCreatedAt(user.getCreatedAt());
            resUser.setUpdatedAt(user.getUpdatedAt());
            return resUser;
        }
    }
}

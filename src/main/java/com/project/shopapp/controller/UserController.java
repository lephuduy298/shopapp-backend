package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.UpdateUserDTO;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.dto.res.ResLogin;
import com.project.shopapp.dto.res.ResRegister;
import com.project.shopapp.dto.res.ResUser;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.error.UserNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.services.UserService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final LocalizationUtils localizationUtils;

    @PostMapping("/register")
    public ResponseEntity<ResRegister> createUser(@Valid @RequestBody UserDTO userDTO) throws PostException, PermissionDenyException {
        if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
            throw new PostException(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
        }

        User user = this.userService.createUser(userDTO);

        ResRegister resRegister = new ResRegister();
        ResRegister.ResUser resUser = ResRegister.ResUser.convertToResUser(user);
        resRegister.setMessage(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY));
        resRegister.setResUser(resUser);
        return ResponseEntity.ok().body(resRegister);
    }

    @PostMapping("/login")
    public ResponseEntity<ResLogin> login (@Valid @RequestBody UserLoginDTO userLoginDTO){
        try {
            String token = this.userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword(), userLoginDTO.getRoleId());


            ResLogin resLogin = ResLogin.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(token)
                    .build();

            return ResponseEntity.ok().body(resLogin);

//        } catch (UserNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//                    ResLogin.builder()
//                            .message(localizationUtils.getLocalizedMessage(MessageKeys.USER_NOT_FOUND, e.getMessage()))
//                            .build()
//            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ResLogin.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED, e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/details")
    public ResponseEntity<ResUser> getUserDetail (@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        String extractedToken = authorizationHeader.substring(7);
        User user = this.userService.getUserDetailByToken(extractedToken);

        return ResponseEntity.ok().body(ResUser.convertToResUser(user));
    }

    @PutMapping("/details/{userId}")
    public ResponseEntity<ResUser> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailByToken(extractedToken);
            // Ensure that the user making the request matches the user being updated
            if (user.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            User updatedUser = userService.updateUser(userId, updatedUserDTO);
            return ResponseEntity.ok(ResUser.convertToResUser(updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

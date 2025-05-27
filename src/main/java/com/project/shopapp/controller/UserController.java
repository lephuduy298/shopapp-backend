package com.project.shopapp.controller;

import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) throws PostException, PermissionDenyException {
        if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
            throw new PostException("Mật khẩu không trùng khớp");
        }

        this.userService.createUser(userDTO);
        return ResponseEntity.ok().body("register success");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody UserLoginDTO userLoginDTO){
        String token = this.userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword());
        return ResponseEntity.ok().body(token);
    }

}

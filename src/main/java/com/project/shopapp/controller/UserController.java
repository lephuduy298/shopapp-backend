package com.project.shopapp.controller;

import com.project.shopapp.components.JwtTokenUtil;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.SocialLoginDTO;
import com.project.shopapp.dto.UpdateUserDTO;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.dto.res.ResLogin;
import com.project.shopapp.dto.res.ResRegister;
import com.project.shopapp.dto.res.ResUser;
import com.project.shopapp.dto.res.ResultPagination;
import com.project.shopapp.error.IndvalidRuntimeException;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.error.UserNotFoundException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.services.AuthService;
import com.project.shopapp.services.UserService;
import com.project.shopapp.utils.MessageKeys;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import static jdk.internal.joptsimple.internal.Messages.message;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    private final UserService userService;

    private final LocalizationUtils localizationUtils;

    private final JwtTokenUtil jwtTokenUtil;

    private final AuthService authService;

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
            User currentUser = this.userService.login(userLoginDTO.getPhoneNumber(), userLoginDTO.getPassword(), userLoginDTO.getRoleId());

            String accessToken = this.jwtTokenUtil.generateAccessToken(currentUser);

            String refreshToken = this.jwtTokenUtil.generateRefreshToken(currentUser);

            this.userService.updateRefreshTokenUser(currentUser, refreshToken);

            ResLogin resLogin = ResLogin.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(accessToken)
                    .build();

            ResponseCookie springCookie = ResponseCookie.from("refresh_token", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenExpiration)
                    .build();

            return  ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(resLogin);

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

    @PutMapping("/{userId}")
    public ResponseEntity<ResUser> updateUserDetails(
            @PathVariable Long userId,
            @RequestBody UpdateUserDTO updatedUserDTO,
            @RequestHeader("Authorization") String authorizationHeader
    ) throws Exception {
            String extractedToken = authorizationHeader.substring(7);
            User user = userService.getUserDetailByToken(extractedToken);
            // Ensure that the user making the request matches the user being updated

            String roleUer = user.getRole().getName().toLowerCase();

            if(!user.getRole().getName().toLowerCase().equals("admin")){
                if (user.getId() != userId) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            User updatedUser = userService.updateUser(userId, updatedUserDTO, roleUer);
            return ResponseEntity.ok(ResUser.convertToResUser(updatedUser));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ResLogin> refreshToken(
            @CookieValue(name = "refresh_token") String refresh_token
    ) throws IndvalidRuntimeException {
        Claims claims = this.jwtTokenUtil.checkValidRefreshToken(refresh_token);

        User currentUser = this.userService.getUserByRefreshTokenAndId(refresh_token, Long.valueOf(claims.getSubject()));

        if(currentUser == null){
            throw new IndvalidRuntimeException("Refresh Token không hợp lệ");
        }

        if(!currentUser.isActive()){
            throw new IndvalidRuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        String accessToken = this.jwtTokenUtil.generateAccessToken(currentUser);

        String newRefreshToken = this.jwtTokenUtil.generateRefreshToken(currentUser);

        this.userService.updateRefreshTokenUser(currentUser, newRefreshToken);

        ResLogin resLogin = ResLogin.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                .token(accessToken)
                .build();

        ResponseCookie springCookie = ResponseCookie.from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return  ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(resLogin);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(
            @CookieValue(name = "refresh_token") String token
    ) throws Exception {

        User user = this.userService.getUserDetailByToken(token);

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        this.userService.updateRefreshTokenUser(user, null);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResUser> getUserById(@PathVariable Long userId,
                                                @RequestHeader("Authorization") String authorizationHeader) throws Exception {

        String extractedToken = authorizationHeader.substring(7);


        User userFromToken = userService.getUserDetailByToken(extractedToken);
        // Ensure that the user making the request matches the user being updated

        String roleUer = userFromToken.getRole().getName().toLowerCase();

        if(!roleUer.equals("admin")){
            if (userFromToken.getId() != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        User userFromId = userService.getUserById(userId);
        ResUser resUser = ResUser.convertToResUser(userFromId);
        return ResponseEntity.ok(resUser);
    }

    @GetMapping
    public ResponseEntity<ResultPagination> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "role_id", required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(name = "is_active", required = false) Boolean isActive
    ) {

        PageRequest pageRequest = PageRequest.of(page > 0 ? page - 1 : page, limit, Sort.by("id").descending());

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        Page<User> users = userService.getAllUsers(roleId, keyword, isActive, pageRequest);

        List<ResUser> resUsers = users.getContent().stream()
                .map(ResUser::convertToResUser)
                .collect(Collectors.toList());

        ResultPagination result = new ResultPagination();
        ResultPagination.Meta meta = new ResultPagination.Meta();

        result.setResult(resUsers);
        meta.setTotalItems(users.getTotalElements());
        meta.setTotalPage(users.getTotalPages());

        result.setMeta(meta);

        return ResponseEntity.ok(result);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<ResUser> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO updateUserDTO) throws Exception {
//        User updatedUser = userService.updateUser(id, updateUserDTO);
//        ResUser resUser = ResUser.convertToResUser(updatedUser);
//        return ResponseEntity.ok(resUser);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/block/{id}")
    public ResponseEntity<Void> blockAndActiveUser(@PathVariable Long id) {
        userService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/social-login")
    public ResponseEntity<String> socialLogin(
            @RequestParam("login_type") String loginType
    ) {
         loginType = loginType.toLowerCase();
        String url = this.authService.generateAuthUrl(loginType);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/auth/social/callback")
    public ResponseEntity<?> callback(
            @RequestBody SocialLoginDTO socialLoginDTO,
            HttpServletRequest request
    ) throws IOException, PermissionDenyException {

        String code = socialLoginDTO.getCode();
        String loginType = socialLoginDTO.getLoginType();

        Map<String, Object> userInfo = this.authService.authenticateAndFetchProfile(code, loginType);

        String email = null;
        String name = null;
        String googleId = "";
        String facebookId = "";

        if ("google".equalsIgnoreCase(loginType)) {
            email = (String) userInfo.get("email");
            name = (String) userInfo.get("name");
            googleId = userInfo.get("sub").toString();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("Không lấy được email từ Google");
            }
        } else if ("facebook".equalsIgnoreCase(loginType)) {
            email = (String) userInfo.get("email"); // có thể null
            name = (String) userInfo.get("name");
            facebookId = userInfo.get("id").toString();

        } else {
            return ResponseEntity.badRequest().body("Login type không hỗ trợ");
        }

        // Tìm user trong DB
        User user = null;
        if ("facebook".equalsIgnoreCase(loginType) && facebookId != null && !facebookId.isEmpty()) {
            user = this.userService.findByFacebookId(facebookId);
        } else if ("google".equalsIgnoreCase(loginType) && googleId != null && !googleId.isEmpty()) {
            user = this.userService.findByGoogleId(googleId);
        }

        // Nếu chưa có thì tạo mới user
        if (user == null) {
            UserDTO userDTO = UserDTO.builder()
                    .fullName(name != null ? name : "")
                    .phoneNumber("") // Social không trả về
                    .email(email  != null ? email : "")
                    .address("")
                    .password("") // không dùng password cho social login
                    .retypePassword("")
                    .dateOfBirth(null)
                    .facebookAccountId(facebookId != null ? facebookId : "")
                    .googleAccountId(googleId != null ? googleId : "")
                    .roleId(1L) // role mặc định
                    .build();
            user = this.userService.createUser(userDTO);
        }

        // Sinh token
        String accessToken = jwtTokenUtil.generateAccessToken(user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(user);
        userService.updateRefreshTokenUser(user, refreshToken);

        ResLogin resLogin = ResLogin.builder()
                .message("Đăng nhập thành công với " + loginType)
                .token(accessToken)
                .build();

        ResponseCookie springCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // ✅ chỉ bật true khi deploy HTTPS
                .sameSite("Lax") // ✅ cho phép cross-site
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(resLogin);
    }

}

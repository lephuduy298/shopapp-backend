package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.UpdateUserDTO;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.dto.UserLoginDTO;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.awt.print.Pageable;
import java.util.List;

public interface IUserService {
    User createUser(UserDTO userDTO) throws PermissionDenyException;
    User login(String phoneNumber, String password, Long roleId);

    User getUserDetailByToken(String extractedToken) throws Exception;

    User updateUser(Long userId, UpdateUserDTO updatedUserDTO, String roleUser) throws Exception;

    void updateRefreshTokenUser(User currentUser, String refreshToken);

    User getUserById(Long userId) throws Exception;

    Page<User> getAllUsers(Long roleId, String keyword, Boolean isActive, PageRequest pageRequest);

    void deleteUser(Long userId) throws Exception;

    void blockUser(Long id);

    User findByEmail(String email);

    User findByFacebookId(String facebookId);

    User findByGoogleId(String googleId);

//    User createUserFromSocial(User user);

//    User getUserByCommentId(Long commentId);
}

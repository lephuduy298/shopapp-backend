package com.project.shopapp.services.iservice;

import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.models.User;

public interface IUserService {
    User createUser(UserDTO userDTO) throws PermissionDenyException;
    String login(String phoneNumber, String password, Long roleId);
}

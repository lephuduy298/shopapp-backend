package com.project.shopapp.services;

import com.project.shopapp.components.JwtTokenUtil;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.UpdateUserDTO;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IUserService;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final LocalizationUtils localizationUtils;

    @Transactional
    @Override
    public User createUser(UserDTO userDTO) throws PermissionDenyException {
        if(this.userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())){
            throw new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.PHONE_ALREADY_EXISTED, userDTO.getPhoneNumber()));
        }
        Role role = this.roleRepository.findById(userDTO.getRoleId()).orElseThrow(() -> new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException(localizationUtils.getLocalizedMessage(MessageKeys.ADMIN_ROLE_NOT_AVAILABLE));
        }
        User user = User
                .builder()
                .fullName(userDTO.getFullName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();
//        Role role = null;
        user.setRole(role);
        user.setActive(true);

        if(user.getFacebookAccountId() == 0 && user.getGoogleAccountId() == 0){
            String password = userDTO.getPassword();
            String passwordEncoder = this.passwordEncoder.encode(password);
            user.setPassword(passwordEncoder);
        }

        return this.userRepository.save(user);
    }

    @Override
    public User login(String phoneNumber, String password, Long roleId) {
        //check exists user
        User currentUser = this.userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD)));

        //check password
        if(currentUser.getFacebookAccountId() == 0 && currentUser.getGoogleAccountId() == 0){
            if(!passwordEncoder.matches(password, currentUser.getPassword())){
                throw new BadCredentialsException(localizationUtils.getLocalizedMessage(MessageKeys.WRONG_PHONE_PASSWORD));
            }
        }

        //check role
        Role role = this.roleRepository.findById(roleId).orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS)));
        if(!roleId.equals(currentUser.getRole().getId())){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }

        if(!currentUser.isActive()) {
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_IS_LOCKED));
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                phoneNumber, password, currentUser.getAuthorities()
        );

        authenticationManager.authenticate(authenticationToken);

        return currentUser;
    }

    @Override
    public User getUserDetailByToken(String extractedToken) throws Exception {
        if(this.jwtTokenUtil.isTokenExpired(extractedToken)){
            throw new Exception("Token is expired");
        }

        String phoneNumber = this.jwtTokenUtil.extractPhoneNumber(extractedToken);

       return this.userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new DataNotFoundException("User not found"));

    }

    @Transactional
    @Override
    public User updateUser(Long userId, UpdateUserDTO updatedUserDTO, String roleUser) throws DataNotFoundException, PostException {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_NOTFOUND)));

        // Check if the phone number is being changed and if it already exists for another user
        String newPhoneNumber = updatedUserDTO.getPhoneNumber();
        if (!existingUser.getPhoneNumber().equals(newPhoneNumber) &&
                userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new DataNotFoundException("Phone number already exists");
        }

        // Update user information based on the DTO
        if (updatedUserDTO.getFullName() != null) {
            existingUser.setFullName(updatedUserDTO.getFullName());
        }
        if (newPhoneNumber != null) {
            existingUser.setPhoneNumber(newPhoneNumber);
        }
        if (updatedUserDTO.getAddress() != null) {
            existingUser.setAddress(updatedUserDTO.getAddress());
        }
        if (updatedUserDTO.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUserDTO.getDateOfBirth());
        }
        if (updatedUserDTO.getFacebookAccountId() > 0) {
            existingUser.setFacebookAccountId(updatedUserDTO.getFacebookAccountId());
        }
        if (updatedUserDTO.getGoogleAccountId() > 0) {
            existingUser.setGoogleAccountId(updatedUserDTO.getGoogleAccountId());
        }

        //check current password
        if(!roleUser.equals("admin")){
            if(updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()) {
                String currentPassword = updatedUserDTO.getCurrentPassword();
                if (currentPassword == null || !passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
                    throw new PostException(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
                }
            }
        }

        //check password match
        if (updatedUserDTO.getPassword() != null && !updatedUserDTO.getPassword().isEmpty()
                && !updatedUserDTO.getPassword().equals(updatedUserDTO.getRetypePassword())) {
            throw new PostException(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH));
        }


        // Update the password if it is provided in the DTO
        if (updatedUserDTO.getPassword() != null
                && !updatedUserDTO.getPassword().isEmpty()) {
            String newPassword = updatedUserDTO.getPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            existingUser.setPassword(encodedPassword);
        }
        //existingUser.setRole(updatedRole);
        // Save the updated user
        return userRepository.save(existingUser);
    }

    @Override
    public void updateRefreshTokenUser(User currentUser, String refreshToken) {
        if(currentUser != null){
            currentUser.setRefreshToken(refreshToken);
            this.userRepository.save(currentUser);

        }
    }

    public User getUserByRefreshTokenAndPhoneNumber(String refreshToken, String subject) {
        return this.userRepository.findByRefreshTokenAndPhoneNumber(refreshToken, subject);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new DataNotFoundException("User not found"));
    }

    @Override
    public Page<User> getAllUsers(Long roleId, String keyword, Boolean isActive, PageRequest pageRequest) {
        return userRepository.findUserWithFilter(roleId, keyword, isActive, pageRequest);
    }

//    @Override
//    public User updateUser(Long id, UpdateUserDTO updateUserDTO) {
//        User existingUser = getUserById(id);
//        if (updateUserDTO.getName() != null) {
//            existingUser.setName(updateUserDTO.getName());
//        }
//        if (updateUserDTO.getEmail() != null) {
//            existingUser.setEmail(updateUserDTO.getEmail());
//        }
//        // Additional fields can be updated here
//        return userRepository.save(existingUser);
//    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new DataNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void blockUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.USER_NOTFOUND)));

        user.setActive(!user.isActive());

        this.userRepository.save(user);
    }
}

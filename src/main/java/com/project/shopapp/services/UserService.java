package com.project.shopapp.services;

import com.project.shopapp.components.JwtTokenUtil;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.error.PostException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IUserService;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final LocalizationUtils localizationUtils;

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
    public String login(String phoneNumber, String password, Long roleId) {
        //check exists user
        User currentUser = this.userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED)));

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

        return jwtTokenUtil.generateToken(currentUser);
    }
}

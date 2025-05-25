package com.project.shopapp.services;

import com.project.shopapp.dto.UserDTO;
import com.project.shopapp.error.DataNotFoundException;
import com.project.shopapp.error.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.services.iservice.IUserService;
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

//    private final AuthenticationManager authenticationManager;

//    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public User createUser(UserDTO userDTO) throws PermissionDenyException {
        if(this.userRepository.existsByPhoneNumber(userDTO.getPhoneNumber())){
            throw new RuntimeException("Phone number already exist");
        }
        Role role = this.roleRepository.findById(userDTO.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        if(role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException("You cannot register an admin account");
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

//    @Override
//    public String login(String phoneNumber, String password) {
//        //check exists user
//        User currentUser = this.userRepository.findByPhoneNumber(phoneNumber)
//                .orElseThrow(() -> new DataNotFoundException("Invalid phone number or password"));
//
//        if(currentUser.getFacebookAccountId() == 0 && currentUser.getGoogleAccountId() == 0){
//            if(!passwordEncoder.matches(password, currentUser.getPassword())){
//                throw new BadCredentialsException("Invalid phone number or password");
//            }
//        }
//
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                phoneNumber, password, currentUser.getAuthorities()
//        );
//
//        authenticationManager.authenticate(authenticationToken);
//
////        return jwtTokenUtil.generateToken(currentUser);
//        return null;
//    }
}

package com.sampleProject.Sample.service.impl;

import com.sampleProject.Sample.entity.User;
import com.sampleProject.Sample.exception.BadRequestException;
import com.sampleProject.Sample.form.LoginForm;
import com.sampleProject.Sample.form.ReferFriendForm;
import com.sampleProject.Sample.form.UserEditForm;
import com.sampleProject.Sample.form.UserForm;
import com.sampleProject.Sample.repository.UserRepository;
import com.sampleProject.Sample.security.JwtUtil;
import com.sampleProject.Sample.service.UserDetailsService;
import com.sampleProject.Sample.service.UserService;
import com.sampleProject.Sample.util.SecurityUtil;
import com.sampleProject.Sample.view.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    UserDetailsService userDetailsService;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SecurityUtil securityUtil;


    @Override
    public UserView registerUser(UserForm userForm)  {

        if (userForm.getFirstName() == null) {
            throw new BadRequestException("First name cannot be null");
        }
        if (userForm.getLastName() == null) {
            throw new BadRequestException("Last name cannot be null");
        }
        if (userForm.getMobileNumber() == null) {
            throw new BadRequestException("Mobile number cannot be null");
        }
        if (userForm.getEmail() == null) {
            throw new BadRequestException("Email cannot be null");
        }
        if (userForm.getPassword() == null) {
            throw new BadRequestException("Password cannot be null");
        }

        Optional existingUser = userRepository.findByEmail(userForm.getEmail());
        if (existingUser.isPresent()) {
            throw new BadRequestException("User already exists");
        }
        User user = new User();
        if (userForm.getParentUuid() != null) {
            System.out.println("Parent uuid: " + userForm.getParentUuid());
            User parentUser = userRepository.findByUuid(UUID.fromString(userForm.getParentUuid()))
                    .orElseThrow(() -> new BadRequestException("Parent user not found"));
            user.setParentEmail(parentUser.getEmail());
        }
        user.setFirstName(userForm.getFirstName());
        user.setLastName(userForm.getLastName());
        user.setMobileNumber(userForm.getMobileNumber());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword());
        user.setStatus(User.Status.ACTIVE.getValue());
        user.setRole(User.Role.USER.getValue());
        String encodedPassword = bCryptPasswordEncoder.encode(userForm.getPassword());
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user); // Save the user first to generate the uuid
        String folderPath = "./profileImages/"; // Folder in the root directory
        String imageName = savedUser.getUuid().toString() + ".jpg"; // Image name with uuid
        Path path = Paths.get(folderPath + imageName);
        try {
            Files.createDirectories(path.getParent()); // Create the directory if it does not exist
            if (userForm.getProfileImage() != null) {
                Files.write(path, userForm.getProfileImage().getBytes());
            } else {
                throw new BadRequestException("Profile image cannot be null");
            }
        }catch (IOException e){
           throw new BadRequestException("Error in saving image");
        }

        savedUser.setProfileImage(path.toString());
        userRepository.save(savedUser);
        UserView userView = new UserView();
        userView.setUserId(savedUser.getId());
        userView.setUuid(savedUser.getUuid());
        userView.setFirstName(savedUser.getFirstName());
        userView.setLastName(savedUser.getLastName());
        userView.setMobileNumber(savedUser.getMobileNumber());
        userView.setProfileImage(savedUser.getProfileImage());
        userView.setEmail(savedUser.getEmail());
        return userView;
    }

    @Override
    public Map<String, Object> userLogin(LoginForm loginForm) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginForm.getEmail(),
                            loginForm.getPassword()
                    )
            );
            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginForm.getEmail());
            final String jwt = jwtUtil.generateToken(loginForm.getEmail());
            final Date jwtExpiryDate = jwtUtil.getExpirationDateFromToken(jwt);

            Map<String, Object> accessTokenMap = new HashMap<>();
            accessTokenMap.put("jwt", jwt);
            accessTokenMap.put("expiry",jwtExpiryDate);


            final String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
            final Date refreshTokenExpiryDate = jwtUtil.getExpirationDateFromToken(refreshToken);
            Map<String, Object> refreshTokenMap = new HashMap<>();
            refreshTokenMap.put("value", refreshToken);
            refreshTokenMap.put("expiry", refreshTokenExpiryDate);
            Map<String, Object> response = new HashMap<>();
            response.put("jwtToken", accessTokenMap);
            response.put("refreshToken", refreshTokenMap);
            response.put("username", userDetails.getUsername());
            response.put("authority", userDetails.getAuthorities().stream().findFirst().get().getAuthority());
            User user = userRepository.findByEmail(loginForm.getEmail()).orElse(null);
            if (user != null) {
                response.put("userId", user.getId());
            }
            return response;

        } catch (Exception e) {
            throw new BadRequestException("Authentication failed");
        }
    }

    @Override
    public Map<String, Object> refreshAuthenticationToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadRequestException("Refresh token is required");
        }

        String username = null;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new BadRequestException("Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
            throw new BadRequestException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateToken(username);
        Date newAccessTokenExpiryDate = jwtUtil.getExpirationDateFromToken(newAccessToken);

        Map<String, Object> accessTokenMap = new HashMap<>();
        accessTokenMap.put("value", newAccessToken);
        accessTokenMap.put("expiry", newAccessTokenExpiryDate);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessTokenMap);
        return response;
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserView editUser(UserEditForm userEditForm) {

        String currentUserEmail = securityUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail).orElseThrow(() -> new BadRequestException("User not found"));

        if (!currentUser.getId().equals(userEditForm.getUserId()) && (currentUser.getRole() != User.Role.ADMIN.getValue()) ){
            throw new BadRequestException("Unauthorized");
        }

        User userToEdit = userRepository.findById(userEditForm.getUserId()).orElseThrow(() -> new BadRequestException("User not found"));

        if (userEditForm.getEmail() != null && (currentUser.getRole()== (User.Role.ADMIN.getValue()))) {
            userToEdit.setEmail(userEditForm.getEmail());
        }

        if (userEditForm.getNewPassword() != null) {
            if ((currentUser.getRole()== (User.Role.ADMIN.getValue())) ) {
                userToEdit.setPassword(bCryptPasswordEncoder.encode(userEditForm.getNewPassword()));
            }
            if(currentUser.getId().equals(userEditForm.getUserId())){
                if (bCryptPasswordEncoder.matches(userEditForm.getExistingPassword(), userToEdit.getPassword())) {
                    userToEdit.setPassword(bCryptPasswordEncoder.encode(userEditForm.getNewPassword()));
                } else {
                    throw new BadRequestException("Invalid existing password");
                }
            }
        }
        if (userEditForm.getFirstName() == null || userEditForm.getFirstName().isEmpty()) {
            throw new BadRequestException("First name is required");
        }else{
            userToEdit.setFirstName(userEditForm.getFirstName());
        }
        if (userEditForm.getLastName() == null || userEditForm.getLastName().isEmpty()) {
            throw new BadRequestException("Last name is required");
        }
        else{
            userToEdit.setLastName(userEditForm.getLastName());
        }
        if (userEditForm.getMobileNumber() == null || userEditForm.getMobileNumber().isEmpty()) {
            throw new BadRequestException("Mobile number is required");
        }else{
            userToEdit.setMobileNumber(userEditForm.getMobileNumber());

        }
        
        
        if (userEditForm.getProfileImage() != null) {
            // handle profile image update
            String folderPath = "./profileImages/"; // Folder in the root directory
            String imageName = userToEdit.getUuid().toString() + ".jpg"; // Image name with uuid
            Path path = Paths.get(folderPath + imageName);

            try {
                Files.createDirectories(path.getParent()); // Create the directory if it does not exist
                Files.write(path, userEditForm.getProfileImage().getBytes());
            }catch (IOException e){
                throw new BadRequestException("Error in saving image");
            }

            userToEdit.setProfileImage(path.toString());

        }

        User savedUser = userRepository.save(userToEdit);

        UserView userView = new UserView();
        userView.setUserId(savedUser.getId());
        userView.setUuid(savedUser.getUuid());
        userView.setFirstName(savedUser.getFirstName());
        userView.setLastName(savedUser.getLastName());
        userView.setMobileNumber(savedUser.getMobileNumber());
        userView.setProfileImage(savedUser.getProfileImage());
        userView.setEmail(savedUser.getEmail());
        return userView;
    }

    public Page<UserView> getActiveUsers(Pageable pageable) {
        Page<User> userList =  userRepository.findByStatusAndRole(User.Status.ACTIVE.getValue(), User.Role.USER.getValue(), pageable);
        List<UserView> userViews = userList.getContent().stream()
                .map(this::convertToUserView)
                .collect(Collectors.toList());
        return new PageImpl<>(userViews, pageable, userList.getTotalElements());

    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public UserView getUserById(Long id) {
        User currentUser = userRepository.findByEmail(securityUtil.getCurrentUserEmail()).orElseThrow(()->new BadRequestException("Current user not found"));
        if (!currentUser.getId().equals(id) || currentUser.getRole()!=(User.Role.ADMIN.getValue())) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BadRequestException("User not found with id: " + id));
            UserView userView = convertToUserView(user);
            return userView;
        }
        else {
            throw new BadRequestException("Unauthorized");
        }
    }



    UserView convertToUserView(User user) {
        UserView userView = new UserView();
        userView.setUserId(user.getId());
        userView.setFirstName(user.getFirstName());
        userView.setLastName(user.getLastName());
        userView.setMobileNumber(user.getMobileNumber());
        userView.setEmail(user.getEmail());
        userView.setUuid(user.getUuid());
        userView.setProfileImage(user.getProfileImage());
        return userView;
    }

    @Override
    public void referFriend(ReferFriendForm referFriendForm) {
        String currentUserEmail = securityUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        String appUrl = "http://10.10.24.45:3000/sign-up?referral=" + currentUser.getUuid();
       // http://10.10.24.45:3000/sign-up
        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(referFriendForm.getRefereeEmail());
        registrationEmail.setSubject("You've been referred to our app!");
        registrationEmail.setText("To register, please click the link below:\n" + appUrl);
        registrationEmail.setFrom("noreply@domain.com");
        mailSender.send(registrationEmail);
    }


}

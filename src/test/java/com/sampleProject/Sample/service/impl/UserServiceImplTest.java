package com.sampleProject.Sample.service.impl;

import com.sampleProject.Sample.entity.User;
import com.sampleProject.Sample.exception.BadRequestException;
import com.sampleProject.Sample.form.LoginForm;
import com.sampleProject.Sample.form.ReferFriendForm;
import com.sampleProject.Sample.form.UserEditForm;
import com.sampleProject.Sample.form.UserForm;
import com.sampleProject.Sample.repository.UserRepository;
import com.sampleProject.Sample.security.JwtUtil;
import com.sampleProject.Sample.service.UserService;
import com.sampleProject.Sample.util.SecurityUtil;
import com.sampleProject.Sample.view.UserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {



    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userServices = mock(UserService.class);
    }

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;


    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private JavaMailSender javaMailSender;


    // Mock dependencies

    private UserService userServices;

    @Captor
    private ArgumentCaptor<User> currentUserArgumentCaptor;

    @Test
    public void testRegisterUser() throws Exception {
        UserForm userForm = new UserForm();
        userForm.setFirstName("Test");
        userForm.setLastName("User");
        userForm.setEmail("test@example.com");
        userForm.setPassword("password");
        userForm.setMobileNumber("1234567890");

        MultipartFile profileImage = new MockMultipartFile("profileImage", "Hello, World!".getBytes());
        userForm.setProfileImage(profileImage);

        User user = new User();
        user.setFirstName(userForm.getFirstName());
        user.setLastName(userForm.getLastName());
        user.setEmail(userForm.getEmail());
        user.setMobileNumber(userForm.getMobileNumber());
        user.setPassword(userForm.getPassword());

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserView expectedUserView = new UserView();
        expectedUserView.setFirstName(user.getFirstName());
        expectedUserView.setLastName(user.getLastName());
        expectedUserView.setEmail(user.getEmail());
        expectedUserView.setMobileNumber(user.getMobileNumber());

        UserView actualUserView = userService.registerUser(userForm);

        assertNotNull(actualUserView);
        assertEquals(expectedUserView.getFirstName(), actualUserView.getFirstName());
        assertEquals(expectedUserView.getLastName(), actualUserView.getLastName());
        assertEquals(expectedUserView.getEmail(), actualUserView.getEmail());
        assertEquals(expectedUserView.getMobileNumber(), actualUserView.getMobileNumber());
        assertEquals(expectedUserView, actualUserView);
    }


    @Test
    public void testUserLoginFailure() {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadRequestException("Authentication failed"));
        assertThrows(BadRequestException.class, () -> userService.userLogin(loginForm));
    }

    @Test
    public void testUserLoginSuccess() {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("password");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                loginForm.getEmail(), loginForm.getPassword());

        String jwtToken = "mockJwtToken";
        String refreshToken = "mockRefreshToken";
        Date jwtExpiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        Date refreshTokenExpiryDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);

        com.sampleProject.Sample.entity.User user = new com.sampleProject.Sample.entity.User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(loginForm.getEmail())).thenReturn(userDetails);
        when(jwtUtil.generateToken(loginForm.getEmail())).thenReturn(jwtToken);
        when(jwtUtil.getExpirationDateFromToken(jwtToken)).thenReturn(jwtExpiryDate);
        when(jwtUtil.generateRefreshToken(userDetails.getUsername())).thenReturn(refreshToken);
        when(jwtUtil.getExpirationDateFromToken(refreshToken)).thenReturn(refreshTokenExpiryDate);
        when(userRepository.findByEmail(loginForm.getEmail())).thenReturn(Optional.of(user));

        Map<String, Object> response = userService.userLogin(loginForm);

        assertNotNull(response);
        assertEquals(jwtToken, ((Map<String, Object>) response.get("jwtToken")).get("jwt"));
        assertEquals(jwtExpiryDate, ((Map<String, Object>) response.get("jwtToken")).get("expiry"));
        assertEquals(refreshToken, ((Map<String, Object>) response.get("refreshToken")).get("value"));
        assertEquals(refreshTokenExpiryDate, ((Map<String, Object>) response.get("refreshToken")).get("expiry"));
        assertEquals(userDetails.getUsername(), response.get("username"));
        assertEquals(userDetails.getAuthorities().stream().findFirst().get().getAuthority(), response.get("authority"));
        assertEquals(user.getId(), response.get("userId"));
    }

    @Test
    public void testRefreshAuthenticationToken_NullOrEmptyToken() {
        assertThrows(BadRequestException.class, () -> userService.refreshAuthenticationToken(null));
        assertThrows(BadRequestException.class, () -> userService.refreshAuthenticationToken(""));
    }

    @Test
    public void testRefreshAuthenticationToken_InvalidToken() {
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        assertThrows(BadRequestException.class, () -> userService.refreshAuthenticationToken("invalidToken"));
    }

    @Test
    public void testRefreshAuthenticationToken_InvalidUserDetails() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("username");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.refreshAuthenticationToken("invalidUserDetails"));
    }

    @Test
    public void testRefreshAuthenticationToken_Success() {
        when(jwtUtil.extractUsername(anyString())).thenReturn("username");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("newAccessToken");
        when(jwtUtil.getExpirationDateFromToken(anyString())).thenReturn(new Date());

        Map<String, Object> response = userService.refreshAuthenticationToken("validToken");

        assertEquals("newAccessToken", ((Map) response.get("accessToken")).get("value"));
    }

    private void setAuthenticatedUser(String email, String role) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("minc@gvfg.bomn5")
                .password("89899")
                .authorities("ROLE_ADMIN")
                .build();
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testEditUserByAdmin() throws Exception {
        when(securityUtil.getCurrentUserEmail()).thenReturn("minc@gvfg.bomn5");
        setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("admin@example.com");
        currentUser.setRole(User.Role.ADMIN.getValue());

        User userToEdit = new User();
        userToEdit.setId(2L);
        userToEdit.setEmail("user@example.com");
        userToEdit.setPassword("existingPassword");

        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUserId(2L);
        userEditForm.setFirstName("NewFirstName");
        userEditForm.setLastName("NewLastName");
        userEditForm.setEmail("newemail@example.com");
        userEditForm.setMobileNumber("1234567890");

        when(userRepository.findByEmail("minc@gvfg.bomn5")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(userToEdit));
        when(userRepository.save(any(User.class))).thenReturn(userToEdit);

        UserView userView = userService.editUser(userEditForm);

        assertNotNull(userView);
        assertEquals(userEditForm.getFirstName(), userView.getFirstName());
        assertEquals(userEditForm.getLastName(), userView.getLastName());
        assertEquals(userEditForm.getEmail(), userView.getEmail());
        assertEquals(userEditForm.getMobileNumber(), userView.getMobileNumber());
    }

    @Test
    public void testUnauthorizedEdit() {
        setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");

        com.sampleProject.Sample.entity.User currentUser = new com.sampleProject.Sample.entity.User();
        currentUser.setId(1L);
        currentUser.setEmail("user@example.com");
        currentUser.setRole(User.Role.USER.getValue());

        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUserId(2L);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));

        assertThrows(BadRequestException.class, () -> userService.editUser(userEditForm));
    }

    @Test
    public void testEditNonExistentUser() {
        setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");

        com.sampleProject.Sample.entity.User currentUser = new com.sampleProject.Sample.entity.User();
        currentUser.setId(1L);
        currentUser.setEmail("admin@example.com");
        currentUser.setRole(User.Role.ADMIN.getValue());

        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUserId(2L);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.editUser(userEditForm));
    }

    @Test
    public void testInvalidExistingPassword() {
        setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");

        com.sampleProject.Sample.entity.User currentUser = new com.sampleProject.Sample.entity.User();
        currentUser.setId(1L);
        currentUser.setEmail("user@example.com");
        currentUser.setRole(User.Role.USER.getValue());
        currentUser.setPassword("existingPassword");

        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUserId(1L);
        userEditForm.setNewPassword("newPassword");
        userEditForm.setExistingPassword("wrongPassword");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(bCryptPasswordEncoder.matches(userEditForm.getExistingPassword(), currentUser.getPassword()))
                .thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.editUser(userEditForm));
    }

    @Test
    public void testUserEditingOwnDetails_Success() {
        // setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");
        when(securityUtil.getCurrentUserEmail()).thenReturn("minc@gvfg.bomn5");
        com.sampleProject.Sample.entity.User currentUser = new com.sampleProject.Sample.entity.User();
        currentUser.setId(1L);
        currentUser.setEmail("minc@gvfg.bomn5");
        currentUser.setRole(User.Role.USER.getValue());
        currentUser.setPassword("existingEncodedPassword");
        currentUser.setFirstName("NewFirstName");
        currentUser.setLastName("NewLastName");
        currentUser.setMobileNumber("1234567890");

        UserEditForm userEditForm = new UserEditForm();
        userEditForm.setUserId(1L);
        userEditForm.setFirstName("NewFirstName");
        userEditForm.setLastName("NewLastName");
        userEditForm.setMobileNumber("1234567890");
        userEditForm.setNewPassword("newPassword");
        userEditForm.setExistingPassword("existingPassword");

        when(userRepository.findByEmail("minc@gvfg.bomn5")).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(bCryptPasswordEncoder.matches(userEditForm.getExistingPassword(), currentUser.getPassword()))
                .thenReturn(true);
        when(bCryptPasswordEncoder.encode(userEditForm.getNewPassword())).thenReturn("newEncodedPassword");

        // Mock the save method to return the updated user
        when(userRepository.save(any(com.sampleProject.Sample.entity.User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.editUser(userEditForm);

        verify(userRepository, times(1)).save(currentUserArgumentCaptor.capture());
        com.sampleProject.Sample.entity.User savedUser = currentUserArgumentCaptor.getValue();

        assertEquals("newEncodedPassword", savedUser.getPassword());
    }
    @Test
    public void testEditUser_WithNonExistingUser_ThrowsException() {
        // Setup mock for SecurityUtil, UserRepository
        SecurityUtil securityUtil = mock(SecurityUtil.class);
        UserRepository userRepository = mock(UserRepository.class);

        // Mocking SecurityUtil to return the current user's email
        when(securityUtil.getCurrentUserEmail()).thenReturn("user@example.com");

        // Mocking UserRepository to return empty for findByEmail indicating user not found
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());


        // Creating UserEditForm
        UserEditForm userEditForm = new UserEditForm();

        // Call the method under test and expect BadRequestException
        assertThrows(BadRequestException.class, () -> userService.editUser(userEditForm));
    }


    @Test
    public void testGetActiveUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER.getValue());
        user.setStatus(User.Status.ACTIVE.getValue());

        List<User> userList = Collections.singletonList(user);
        Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());

        when(userRepository.findByStatusAndRole(User.Status.ACTIVE.getValue(), User.Role.USER.getValue(), pageable))
                .thenReturn(userPage);

        // Act
        Page<UserView> userViewPage = userService.getActiveUsers(pageable);

        // Assert
        assertEquals(userPage.getTotalElements(), userViewPage.getTotalElements());
        assertEquals(user.getId(), userViewPage.getContent().get(0).getUserId());
        assertEquals(user.getEmail(), userViewPage.getContent().get(0).getEmail());

        verify(userRepository, times(1)).findByStatusAndRole(User.Status.ACTIVE.getValue(), User.Role.USER.getValue(),
                pageable);
    }

    @Test
    public void testGetUserById() {
        when(securityUtil.getCurrentUserEmail()).thenReturn("minc@gvfg.bomn5");
        Long id = 1L;

        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");
        user.setRole(User.Role.USER.getValue());

        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setEmail("minc@gvfg.bomn5");
        currentUser.setRole(User.Role.ADMIN.getValue());

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("minc@gvfg.bomn5")).thenReturn(Optional.of(currentUser));

        setAuthenticatedUser("minc@gvfg.bomn5", "ADMIN");

        // Act
        UserView userView = userService.getUserById(id);

        // Assert
        assertEquals(user.getId(), userView.getUserId());
        assertEquals(user.getEmail(), userView.getEmail());

        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).findByEmail("minc@gvfg.bomn5");
    }



    @Test
    public void testConvertToUserView() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setMobileNumber("1234567890");
        user.setEmail("john.doe@example.com");
        user.setProfileImage("profile.jpg");

        // Act
        UserView userView = userService.convertToUserView(user);

        // Assert
        assertEquals(user.getId(), userView.getUserId());
        assertEquals(user.getFirstName(), userView.getFirstName());
        assertEquals(user.getLastName(), userView.getLastName());
        assertEquals(user.getMobileNumber(), userView.getMobileNumber());
        assertEquals(user.getEmail(), userView.getEmail());
        assertEquals(user.getProfileImage(), userView.getProfileImage());
    }

    @Test
    public void testReferFriend() {
        when(securityUtil.getCurrentUserEmail()).thenReturn("minc@gvfg.bomn5");
        // Arrange
        String currentUserEmail = "minc@gvfg.bomn5";
        User currentUser = new User();
        currentUser.setEmail(currentUserEmail);
        currentUser.setUuid(UUID.fromString("82905d05-6089-42b8-a1a6-bd4bf8dbbbc5"));
        ReferFriendForm referFriendForm = new ReferFriendForm();
        referFriendForm.setRefereeEmail("friend@example.com");

        when(userRepository.findByEmail(currentUserEmail)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("friend@example.com")).thenReturn(Optional.of(new User()));

        // Mock the authentication context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        SecurityContextHolder.setContext(securityContext);
        // Act
        userService.referFriend(referFriendForm);
        // Assert

        // Assert
        ArgumentCaptor<SimpleMailMessage> mailMessageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(mailMessageCaptor.capture());

        SimpleMailMessage sentEmail = mailMessageCaptor.getValue();
        assertEquals("friend@example.com", sentEmail.getTo()[0]);
        assertEquals("You've been referred to our app!", sentEmail.getSubject());
        assertTrue(sentEmail.getText().contains("http://10.10.24.45:3000/sign-up?referral=" + currentUser.getUuid().toString()));
        assertEquals("noreply@domain.com", sentEmail.getFrom());
    }

 }




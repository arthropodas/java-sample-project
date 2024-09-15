package com.sampleProject.Sample.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sampleProject.Sample.form.ReferFriendForm;
import com.sampleProject.Sample.form.UserEditForm;
import com.sampleProject.Sample.form.UserForm;
import com.sampleProject.Sample.service.UserService;
import com.sampleProject.Sample.view.UserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testRegisterUser() throws Exception {
        UserForm userForm = new UserForm();
        // set properties for userForm

        UserView userView = new UserView();
        // set properties for userView
        // cannot resolve referral

        when(userService.registerUser(any(UserForm.class))).thenReturn(userView);

        mockMvc.perform(post("/users/register")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("referral", "referralValue")
                .flashAttr("userForm", userForm))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(userView)));
    }


    @Test
    public void testEditUser() throws Exception {
        UserEditForm userEditForm = new UserEditForm();
        // set properties for userEditForm

        UserView userView = new UserView();
        // set properties for userView

        when(userService.editUser(any(UserEditForm.class))).thenReturn(userView);

        mockMvc.perform(put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userEditForm)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(userView)));
    }


    @Test
    public void testGetActiveUsers()  {


    }

    @Test
    void getActiveUsers_ValidSortAttribute() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        List<UserView> userViewList = new ArrayList<>();
        Page<UserView> userViewPage = new PageImpl<>(userViewList);
        Mockito.when(userService.getActiveUsers(any(Pageable.class))).thenReturn(userViewPage);
        // Act
        ResponseEntity<Page<UserView>> response = userController.getActiveUsers(pageable);

        // Assert
        assertEquals(userViewPage, response.getBody());
        Mockito.verify(userService).getActiveUsers(any(Pageable.class));
    }


    @Test
    public void testGetUserById() throws Exception {
        UserView userView = new UserView();
        // set properties for userView

        when(userService.getUserById(any(Long.class))).thenReturn(userView);

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(userView)));
    }

    @Test
    public void testReferFriend() throws Exception {
        ReferFriendForm referFriendForm = new ReferFriendForm();
        // set properties for referFriendForm

        mockMvc.perform(post("/users/refer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(referFriendForm)))
                .andExpect(status().isOk());
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

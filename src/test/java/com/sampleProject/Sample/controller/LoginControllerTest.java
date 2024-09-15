package com.sampleProject.Sample.controller;

import com.sampleProject.Sample.form.LoginForm;
import com.sampleProject.Sample.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginControllerTest {

    @InjectMocks
    private LoginController loginController;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    public void testCreateAuthenticationToken() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("testemail@gm.com");
        loginForm.setPassword("testPassword");

        Map<String, Object> response = new HashMap<>();
        response.put("token", "testToken");

        when(userService.userLogin(any(LoginForm.class))).thenReturn(response);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUser\", \"password\":\"testPassword\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testRefreshAuthenticationToken() throws Exception {
        String refreshToken = "testRefreshToken";

        Map<String, Object> response = new HashMap<>();
        response.put("token", "newToken");

        when(userService.refreshAuthenticationToken(anyString())).thenReturn(response);

        mockMvc.perform(post("/login/refresh")
                        .param("refreshToken", refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
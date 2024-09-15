package com.sampleProject.Sample.controller;

import com.sampleProject.Sample.form.LoginForm;
import com.sampleProject.Sample.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginForm loginForm) throws Exception {
        Map<String, Object>  response =userService.userLogin(loginForm);
        return ResponseEntity.ok(response);
    }
    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(@RequestParam String refreshToken) throws Exception {
        Map<String, Object>  response =userService.refreshAuthenticationToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    


}

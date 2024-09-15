package com.sampleProject.Sample.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Log the exception
        System.out.println("Access denied exception: " + accessDeniedException.getMessage());

        // Redirect user to access denied page
        response.sendRedirect(request.getContextPath() + "/access-denied");
    }

}

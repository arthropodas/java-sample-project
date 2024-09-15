package com.sampleProject.Sample.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;


public class JwtRequestLoggingFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // Log roles
            authentication.getAuthorities().forEach(authority -> {
                logger.info("Role: " + authority.getAuthority());
            });
        } else {
            logger.debug("No authentication found in SecurityContextHolder");
        }

        // Continue with the filter chain
        chain.doFilter(request, response);
    }
}

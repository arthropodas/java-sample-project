package com.sampleProject.Sample.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtilTest {

    private SecurityUtil securityUtil;

    @Before
    public void setUp() {
        securityUtil = new SecurityUtil();
    }

    @Test
    public void testGetCurrentUserEmail_WithUserDetails() {
        // Mock the UserDetails, Authentication, and SecurityContext
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // Stubbing the methods to return the expected values
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Setting the SecurityContext to our mocked version
        SecurityContextHolder.setContext(securityContext);

        // Call the method under test
        String email = securityUtil.getCurrentUserEmail();

        // Assert the expected outcome
        assertEquals("user@example.com", email);
    }

    @Test
    public void testGetCurrentUserEmail_WithNonUserDetails() {
        // Mock the Authentication and SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // Stubbing the methods to return the expected values
        when(authentication.getPrincipal()).thenReturn(new Object());
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Setting the SecurityContext to our mocked version
        SecurityContextHolder.setContext(securityContext);

        // Call the method under test
        String email = securityUtil.getCurrentUserEmail();

        // Assert the expected outcome
        assertEquals(null, email);
    }
}

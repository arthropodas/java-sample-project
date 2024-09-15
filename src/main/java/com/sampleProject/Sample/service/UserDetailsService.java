package com.sampleProject.Sample.service;


import com.sampleProject.Sample.form.LoginForm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

}

package com.sampleProject.Sample.view;

public class LoginView {


    private final String jwt;

    public LoginView(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}

package com.sampleProject.Sample.view;

public class ResponseView {

    private String errorMessage;

    public ResponseView(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ResponseView(String errorMessage, String errorCode) {
        this.errorMessage = errorMessage;
    }
}

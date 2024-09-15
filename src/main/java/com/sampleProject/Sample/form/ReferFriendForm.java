package com.sampleProject.Sample.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class ReferFriendForm {

    @NotBlank
    @Email
    private String refereeEmail;

    public @NotBlank @Email String getRefereeEmail() {
        return refereeEmail;
    }

    public void setRefereeEmail(@NotBlank @Email String refereeEmail) {
        this.refereeEmail = refereeEmail;
    }
}

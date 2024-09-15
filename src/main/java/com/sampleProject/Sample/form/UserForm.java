package com.sampleProject.Sample.form;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.springframework.web.multipart.MultipartFile;

public class UserForm {


    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp="(^$|[0-9]{10})", message = "Mobile number should be valid")
    private String mobileNumber;

    @Email(message = "Parent email should be valid")
    private String parentUuid;

    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Profile image is mandatory")
    private MultipartFile profileImage;



    public @NotBlank(message = "First name is mandatory") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is mandatory") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Last name is mandatory") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is mandatory") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "Password is mandatory") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is mandatory") String password) {
        this.password = password;
    }


    public @NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(@NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public @Email(message = "Parent email should be valid") String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(@Email(message = "Parent email should be valid") String parentUuid) {
        this.parentUuid = parentUuid;
    }
 @NotBlank(message = "Profile image is mandatory")
 public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(@NotBlank(message = "Profile image is mandatory") MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public @Email(message = "Email should be valid") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Email should be valid") String email) {
        this.email = email;
    }
}

package com.sampleProject.Sample.view;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Objects;
import java.util.UUID;


public class UserView {

    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp="(^$|[0-9]{10})", message = "Mobile number should be valid")
    private String mobileNumber;

    private String profileImage;

    private Long userId;

    private UUID uuid;

   // private String password;



    @Email(message = "email should be valid")
    private String email;

    public @NotBlank(message = "Last name is mandatory") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is mandatory") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "First name is mandatory") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is mandatory") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(@NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public @Email(message = "email should be valid") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "email should be valid") String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserView userView = (UserView) o;
        return Objects.equals(getFirstName(), userView.getFirstName()) &&
                Objects.equals(getLastName(), userView.getLastName()) &&
                Objects.equals(getEmail(), userView.getEmail()) &&
                Objects.equals(getMobileNumber(), userView.getMobileNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getEmail(), getMobileNumber());
    }


}

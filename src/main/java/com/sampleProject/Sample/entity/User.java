package com.sampleProject.Sample.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {


    public enum Role {
        ADMIN((byte) 0),
        USER((byte) 1);
        private final byte value;

        Role(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static Role fromValue(byte value) {
            for (Role role : Role.values()) {
                if (role.getValue() == value) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Invalid role value: " + value);
        }

        @Override
        public String toString() {
            return "ROLE_" + name();
        }
    }

    public enum Status {
    ACTIVE((byte) 1),
    INACTIVE((byte) 0);

    private byte value;

    Status(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    @Type(type = "uuid-char")
    private UUID uuid = UUID.randomUUID();

    @NotBlank(message = "First name is mandatory")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Column(name = "last_name")
    private String lastName;

    @NotBlank(message = "Password is mandatory")
    @Column(name = "password")
    private String password;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp="(^$|[0-9]{10})", message = "Mobile number should be valid")
    @Column(name = "mobile_number")
    private String mobileNumber;

    @Email(message ="email should be valid")
    @Column(name = "email")
    private String email;

    @Email(message = "Parent email should be valid")
    @Column(name = "parent_email")
    private String parentEmail;

    @Column(name = "role")
    private byte role;

    @Column(name = "status")
    private byte status;

    @NotBlank(message = "Profile image is mandatory")
    @Column(name = "profile_image")
    private String profileImage;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @PrePersist
    protected void onCreate(){
        createDate = new Date();
    }

    @PreUpdate
    protected void onUpdate(){
        updateDate = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public @NotBlank(message = "First name is mandatory") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is mandatory") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "Password is mandatory") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is mandatory") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Last name is mandatory") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is mandatory") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(@NotBlank(message = "Mobile number is mandatory") @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number should be valid") String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public @Email(message = "Parent email should be valid") String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(@Email(message = "Parent email should be valid") String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public byte getRole() {
        return role;
    }

    public void setRole(byte role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public @Email(message = "email should be valid") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "email should be valid") String email) {
        this.email = email;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }


}

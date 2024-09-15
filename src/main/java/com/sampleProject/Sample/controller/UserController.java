package com.sampleProject.Sample.controller;

import com.sampleProject.Sample.entity.User;
import com.sampleProject.Sample.form.ReferFriendForm;
import com.sampleProject.Sample.form.UserEditForm;
import com.sampleProject.Sample.form.UserForm;
import com.sampleProject.Sample.repository.UserRepository;
import com.sampleProject.Sample.service.UserService;
import com.sampleProject.Sample.view.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepo;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserView> registerUser(@ModelAttribute UserForm userForm, @RequestParam(value = "referral", required = false) String referral) {
        if(referral != null) {
          userForm.setParentUuid(referral);
        }
        UserView registeredUser = userService.registerUser(userForm);
        return ResponseEntity.ok(registeredUser);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserView> editUser(@PathVariable Long userId, UserEditForm userEditForm) {
        System.out.println(userEditForm.getFirstName());
        userEditForm.setUserId(userId);
        UserView updatedUser = userService.editUser(userEditForm);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<Page<UserView>> getActiveUsers(Pageable pageable) {
        if (pageable.getSort().isEmpty()||!isValidSortAttribute(pageable.getSort().get().toString())) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id"));
        }
        Page<UserView> activeUsers = userService.getActiveUsers(pageable);
        return ResponseEntity.ok(activeUsers);
    }
    
    private boolean isValidSortAttribute(String sortAttribute) {
        String[] validAttributes = {"firstName", "lastName", "mobileNumber", "id", "uuid", "email"};
        for (String attribute : validAttributes) {
            if (attribute.equals(sortAttribute)) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserView> getUserById(@PathVariable Long id) {
        UserView user = userService.getUserById(id);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/refer")
    public ResponseEntity<?> referFriend(@Valid @RequestBody ReferFriendForm referFriendForm) {
        userService.referFriend(referFriendForm);
        return ResponseEntity.ok().build();
    }


    

}

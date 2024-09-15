package com.sampleProject.Sample.service;

import com.sampleProject.Sample.form.LoginForm;
import com.sampleProject.Sample.form.ReferFriendForm;
import com.sampleProject.Sample.form.UserEditForm;
import com.sampleProject.Sample.form.UserForm;
import com.sampleProject.Sample.view.UserView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface UserService {

    UserView registerUser(UserForm userForm) ;

    Map<String, Object> userLogin(LoginForm loginForm);

    Map<String, Object> refreshAuthenticationToken(String refreshToken);

    UserView editUser(UserEditForm userEditForm);

    Page<UserView> getActiveUsers(Pageable pageable);

    UserView getUserById(Long id);

    void referFriend(ReferFriendForm referFriendForm);


}

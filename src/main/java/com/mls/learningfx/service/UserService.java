
package com.mls.learningfx.service;

import com.mls.learningfx.model.User;
import com.mls.learningfx.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    private static User loggedInUser;

    public Boolean login(String username, String password) {
        User user = userRepository.login(username, password);

        if (user != null) {
            loggedInUser = user;
            return true;
        }

        loggedInUser = null;
        return false;
    }

    public static Integer getLoggedInUserId() {
        if (loggedInUser != null) {
            return loggedInUser.getId().intValue();
        }

        return null;
    }
}

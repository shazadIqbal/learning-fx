
package com.mls.learningfx.service;

import com.mls.learningfx.model.Role;
import com.mls.learningfx.model.User;
import com.mls.learningfx.repository.UserRepository;

import java.util.List;

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

    public static boolean isAdmin() {
        return Role.ADMIN.equals(loggedInUser.getRole());
    }

    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public boolean addUser(User user) {
        if (!isValid(user)) {
            return false;
        }

        return userRepository.addUser(user);
    }

    public boolean updateUser(User user) {
        if (!isValid(user)) {
            return false;
        }

        return userRepository.updateUser(user);
    }

    // admin cannot delete their own account
    public boolean deleteUser(int id) {
        if (id == getLoggedInUserId()) {
            return false;
        }

        return userRepository.deleteUser(id);
    }

    private boolean isValid(User user) {
        return user.getName() != null && !user.getName().isBlank()
                && user.getEmail() != null && !user.getEmail().isBlank()
                && user.getPassword() != null && !user.getPassword().isBlank()
                && user.getRole() != null;
    }
}

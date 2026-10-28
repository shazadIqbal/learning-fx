package com.mls.learningfx.service;

import com.mls.learningfx.model.User;
import com.mls.learningfx.repository.UserRepository;

public class UserService {


    private final UserRepository userRepository = new UserRepository();

    public Boolean login(String username,String password){
        User user = userRepository.login(username,password);
        if(user != null){
          return true;
        }

        return false;
    }

}

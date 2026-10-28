package com.mls.learningfx.repository;

import com.mls.learningfx.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository extends BaseRepository {


    public List<User> getAllUsers(){
        List<User> userList = new ArrayList<>();
        Connection connection = getConnection();

        String sql = "select * from app_user";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userList.add(new User(
                        (long) rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userList;
    }

    public User login(String email,String password){
        Connection connection = getConnection();

        String sql = "select * from app_user where email = ? and password = ?";

        try {

            ResultSet rs = executeQuery(connection,sql,email,password);
            while (rs.next()){
                return User.builder()
                                .id((long) rs.getInt("id"))
                                .name(rs.getString("name"))
                                .contact( rs.getString("contact"))
                                .email(rs.getString("email"))
                                .build();


            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

}

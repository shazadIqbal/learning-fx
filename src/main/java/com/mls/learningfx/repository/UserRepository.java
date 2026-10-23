package com.mls.learningfx.repository;

import com.mls.learningfx.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository  {


    private Connection getConnection(){
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/learning",
                    "postgres",
                    "root"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

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
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1,email);
            ps.setObject(2,password);

            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                return new User(
                        (long) rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

}

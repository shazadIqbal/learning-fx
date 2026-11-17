
package com.mls.learningfx.repository;

import com.mls.learningfx.model.Role;
import com.mls.learningfx.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository extends BaseRepository {


    public List<User> getAllUsers(){
        List<User> userList = new ArrayList<>();
        Connection connection = getConnection();

        String sql = "select u.*, r.name as role_name from users u " +
                "join user_role r on u.role_id = r.id " +
                "where u.is_active = true " +
                "order by u.id";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                userList.add(new User(
                        (long) rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role_name"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userList;
    }

    public User login(String email,String password){
        Connection connection = getConnection();

        String sql = "select u.*, r.name as role_name from users u " +
                "join user_role r on u.role_id = r.id " +
                "where u.email = ? and u.password = ? and u.is_active = true";

        try {

            ResultSet rs = executeQuery(connection,sql,email,password);
            while (rs.next()){
                return User.builder()
                                .id((long) rs.getInt("id"))
                                .name(rs.getString("name"))
                                .contact( rs.getString("contact"))
                                .email(rs.getString("email"))
                                .role(Role.valueOf(rs.getString("role_name")))
                                .build();


            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;

    }

    public boolean addUser(User user) {

        String sql = "INSERT INTO users " +
                "(name, contact, email, password, role_id) " +
                "VALUES (?, ?, ?, ?, (SELECT id FROM user_role WHERE name = ?))";

        int rows = executeUpdate(sql, user.getName(), user.getContact(),
                user.getEmail(), user.getPassword(), user.getRole().name());

        return rows > 0;
    }

    public boolean updateUser(User user) {

        String sql = "UPDATE users SET name = ?, contact = ?, email = ?, " +
                "password = ?, role_id = (SELECT id FROM user_role WHERE name = ?) " +
                "WHERE id = ?";

        int rows = executeUpdate(sql, user.getName(), user.getContact(),
                user.getEmail(), user.getPassword(), user.getRole().name(),
                user.getId().intValue());

        return rows > 0;
    }

    // hide the user, do not remove it
    public boolean deleteUser(int id) {

        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";

        int rows = executeUpdate(sql, id);

        return rows > 0;
    }


}

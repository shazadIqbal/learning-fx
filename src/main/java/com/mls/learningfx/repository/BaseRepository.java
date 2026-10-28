package com.mls.learningfx.repository;

import java.sql.*;

public abstract class BaseRepository {

    protected Connection connection;

    public BaseRepository() {
        this.connection = getConnection();
    }

    // Shared database connection
    protected Connection getConnection() {
        try {

            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/learning",
                    "postgres",
                    "root");

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database connection failed", e
            );
        }
    }

    protected int executeUpdate(String sql,Object... params){
        try (Connection conn = getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Database operation failed", e);
        }
    }

    protected ResultSet executeQuery(
            Connection conn, String sql, Object... params)
            throws SQLException {

        PreparedStatement stmt =
                conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);

        }

        return stmt.executeQuery();
    }


}

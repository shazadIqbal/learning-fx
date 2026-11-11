package com.mls.learningfx.repository;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class SaleRepository extends BaseRepository {

    public Integer findCustomerIdByPhone(String phone) {
        String sql = "SELECT id FROM customer WHERE contact = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to search customer: " + e.getMessage(), e);
        }

        return null;
    }

    public int checkout(Integer customerId,
                        Customer newCustomer,
                        Integer userId,
                        double totalPrice,
                        String paymentMode,
                        List<Item> items) {

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            if (customerId == null && newCustomer != null) {
                customerId = insertCustomer(conn, newCustomer.getName(), newCustomer.getContact());
            }

            int paymentTypeId = getOrCreatePaymentTypeId(conn, paymentMode);

            int saleId = insertSale(conn, customerId, userId, totalPrice, paymentTypeId);

            insertSaleItems(conn, saleId, items);

            updateStockForItems(conn, items);

            conn.commit();
            return saleId;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private int insertCustomer(Connection conn, String name, String contact) throws SQLException {
        String sql = "INSERT INTO customer (name, contact) VALUES (?, ?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, contact);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Failed to create customer record.");
    }

    private int getOrCreatePaymentTypeId(Connection conn, String mode) throws SQLException {
        String selectSql = "SELECT id FROM payment_type WHERE LOWER(mode) = LOWER(?)";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, mode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        String insertSql = "INSERT INTO payment_type (mode) VALUES (?) RETURNING id";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, mode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Failed to resolve payment_type_id for mode: " + mode);
    }

    private int insertSale(Connection conn, int customerId, Integer userId, double totalPrice, int paymentTypeId) throws SQLException {
        String sql = "INSERT INTO sale (cus_id, user_id, date, total_price, payment_type_id) VALUES (?, ?, ?, ?, ?) RETURNING s_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setObject(2, userId, Types.INTEGER);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBigDecimal(4, BigDecimal.valueOf(totalPrice));
            ps.setInt(5, paymentTypeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("s_id");
                }
            }
        }
        throw new SQLException("Failed to insert sale record.");
    }

    private void insertSaleItems(Connection conn, int saleId, List<Item> items) throws SQLException {
        String sql = "INSERT INTO sale_item (s_id, p_id, qty) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Item item : items) {
                ps.setInt(1, saleId);
                ps.setInt(2, item.getProductId());
                ps.setInt(3, item.getQuantity());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to insert sale_item for product: " + item.getName());
                }
            }
        }
    }

    private void updateStockForItems(Connection conn, List<Item> items) throws SQLException {
        String sql = "UPDATE stock SET qty = qty - ?, date = ? WHERE product_id = ?";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Item item : items) {
                ps.setInt(1, item.getQuantity());
                ps.setTimestamp(2, now);
                ps.setInt(3, item.getProductId());
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Stock record not found for product: " + item.getName());
                }
            }
        }
    }
}
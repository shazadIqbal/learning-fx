package com.mls.learningfx.repository;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import com.mls.learningfx.model.CustomerSaleHistory;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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

    // GET CUSTOMER SALE HISTORY
    public List<CustomerSaleHistory> getCustomerSaleHistory(
            Integer customerId) {

        List<CustomerSaleHistory> historyList = new ArrayList<>();

        String sql =
                "SELECT s.s_id, s.date, pt.mode AS payment_mode, " +
                        "s.total_price, " +
                        "COALESCE(STRING_AGG(" +
                        "p.name || ' x ' || si.qty::TEXT, ', ' ORDER BY p.name" +
                        "), 'No items') AS purchased_items " +
                        "FROM sale s " +
                        "LEFT JOIN payment_type pt ON s.payment_type_id = pt.id " +
                        "LEFT JOIN sale_item si ON s.s_id = si.s_id " +
                        "LEFT JOIN product p ON si.p_id = p.id " +
                        "WHERE s.cus_id = ? " +
                        "GROUP BY s.s_id, s.date, pt.mode, s.total_price " +
                        "ORDER BY s.date DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String date = rs.getTimestamp("date")
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy, hh:mm a"));

                    historyList.add(
                            CustomerSaleHistory.builder()
                                    .saleId(rs.getInt("s_id"))
                                    .purchaseDate(date)
                                    .paymentMode(rs.getString("payment_mode"))
                                    .totalPrice(rs.getBigDecimal("total_price"))
                                    .purchasedItems(rs.getString("purchased_items"))
                                    .build()
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to fetch customer sale history", e);
        }

        return historyList;
    }

    // GET TOTAL CUSTOMER SPENDINGS
    public BigDecimal getCustomerTotalSpendings(Integer customerId) {

        String sql =
                "SELECT COALESCE(SUM(total_price), 0) AS total_spending " +
                        "FROM sale WHERE cus_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("total_spending");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to calculate customer spendings", e);
        }

        return BigDecimal.ZERO;
    }

    // get all items of a sale
    public List<Item> getSaleItems(int saleId) {

        List<Item> items = new ArrayList<>();

        String sql = "SELECT si.p_id, p.name, si.qty, p.sell_price " +
                "FROM sale_item si " +
                "JOIN product p ON si.p_id = p.id " +
                "WHERE si.s_id = ? " +
                "ORDER BY p.name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int qty = rs.getInt("qty");
                    double price = rs.getDouble("sell_price");

                    items.add(
                            Item.builder()
                                    .productId(rs.getInt("p_id"))
                                    .name(rs.getString("name"))
                                    .quantity(qty)
                                    .price(price)
                                    .subTotal(qty * price)
                                    .build()
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sale items", e);
        }

        return items;
    }

    // Return items
    public void returnItems(int saleId,
                            List<Item> soldItems,
                            List<Integer> returnQtys,
                            double refundAmount,
                            Integer userId) {

        Connection conn = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (int i = 0; i < soldItems.size(); i++) {

                Item item = soldItems.get(i);
                int returnQty = returnQtys.get(i);

                if (returnQty == 0) {
                    continue;
                }

                if (returnQty == item.getQuantity()) {
                    // if all items are returned then remove from sale
                    runUpdate(conn,
                            "DELETE FROM sale_item WHERE s_id = ? AND p_id = ?",
                            saleId, item.getProductId());
                } else {
                    runUpdate(conn,
                            "UPDATE sale_item SET qty = qty - ? WHERE s_id = ? AND p_id = ?",
                            returnQty, saleId, item.getProductId());
                }

                // returned items go back in stock
                runUpdate(conn,
                        "INSERT INTO stock (qty, created_by, product_id) VALUES (?, ?, ?)",
                        returnQty, userId, item.getProductId());
            }

            // total can not go below 0
            runUpdate(conn,
                    "UPDATE sale SET total_price = GREATEST(total_price - ?, 0) WHERE s_id = ?",
                    BigDecimal.valueOf(refundAmount), saleId);

            conn.commit();

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

    private void runUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }

}
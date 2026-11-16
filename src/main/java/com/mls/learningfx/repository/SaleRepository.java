package com.mls.learningfx.repository;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import com.mls.learningfx.model.CustomerSaleHistory;
import com.mls.learningfx.model.SaleRecord;
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
        String sql = "UPDATE stock SET qty = qty - ?, date = ? " +
                "WHERE product_id = ? AND qty >= ?";
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Item item : items) {
                ps.setInt(1, item.getQuantity());
                ps.setTimestamp(2, now);
                ps.setInt(3, item.getProductId());
                ps.setInt(4, item.getQuantity());
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

    // GET COMPLETE SALE RECORD BY SALE ID
    public SaleRecord getSaleRecordById(int saleId) {

        String sql =
                "SELECT s.s_id, s.date, s.total_price, " +
                        "COALESCE(pt.mode, 'N/A') AS payment_mode, " +
                        "c.id AS cus_id, COALESCE(c.name, 'Walk-in Customer') AS cus_name, COALESCE(c.contact, 'N/A') AS cus_contact, " +
                        "COALESCE(u.name, 'N/A') AS sold_by " +
                        "FROM sale s " +
                        "LEFT JOIN customer c ON s.cus_id = c.id " +
                        "LEFT JOIN payment_type pt ON s.payment_type_id = pt.id " +
                        "LEFT JOIN users u ON s.user_id = u.id " +
                        "WHERE s.s_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String formattedDate = rs.getTimestamp("date") != null
                            ? rs.getTimestamp("date")
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                            : "N/A";

                    Customer customer = Customer.builder()
                            .id((Integer) rs.getObject("cus_id"))
                            .name(rs.getString("cus_name"))
                            .contact(rs.getString("cus_contact"))
                            .build();

                    List<Item> items = getSaleItemsBySaleId(conn, saleId);

                    return SaleRecord.builder()
                            .saleId(rs.getInt("s_id"))
                            .saleDate(formattedDate)
                            .paymentMode(rs.getString("payment_mode"))
                            .totalPrice(rs.getBigDecimal("total_price"))
                            .customer(customer)
                            .soldBy(rs.getString("sold_by"))
                            .items(items)
                            .build();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch sale record: " + e.getMessage(), e);
        }

        return null;
    }

    private List<Item> getSaleItemsBySaleId(Connection conn, int saleId) throws SQLException {

        List<Item> items = new ArrayList<>();

        String sql =
                "SELECT p.id AS product_id, p.name, si.qty, p.sell_price, " +
                        "(si.qty * p.sell_price) AS sub_total " +
                        "FROM sale_item si " +
                        "JOIN product p ON si.p_id = p.id " +
                        "WHERE si.s_id = ? " +
                        "ORDER BY p.id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(
                            Item.builder()
                                    .productId(rs.getInt("product_id"))
                                    .name(rs.getString("name"))
                                    .quantity(rs.getInt("qty"))
                                    .price(rs.getDouble("sell_price"))
                                    .subTotal(rs.getDouble("sub_total"))
                                    .build()
                    );
                }
            }
        }

        return items;
    }

}


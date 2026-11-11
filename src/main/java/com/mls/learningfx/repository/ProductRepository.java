
package com.mls.learningfx.repository;

import com.mls.learningfx.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository extends BaseRepository {

    public List<Product> getAllProducts() {
        String sql = "SELECT p.*, c.name AS category_name, " +
                "u.name AS created_by_name, " +
                "(SELECT SUM(qty) FROM stock WHERE stock.product_id = p.id) AS quantity " +
                "FROM product p " +
                "LEFT JOIN product_category c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.created_by = u.id " +
                "ORDER BY p.id";

        return loadProducts(sql, null);
    }

    // save product and get its id
    public Integer addProduct(String name, Integer categoryId,
                              Double price, Double sellPrice,
                              Integer createdBy, String barcode) {

        String sql = "INSERT INTO product " +
                "(name, category_id, price, sell_price, created_by, barcode) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = getConnection();
             ResultSet rs = executeQuery(conn, sql, name, categoryId,
                     price, sellPrice, createdBy, barcode)) {

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public boolean updateProduct(int id, String name,
                                 Integer categoryId, Double price,
                                 Double sellPrice, String barcode) {

        String sql = "UPDATE product SET name = ?, " +
                "category_id = ?, price = ?, sell_price = ?, " +
                "barcode = ? WHERE id = ?";

        int rows = executeUpdate(sql, name, categoryId,
                price, sellPrice, barcode, id);

        return rows > 0;
    }

    public boolean deleteProduct(int id) {

        String sql = "DELETE FROM product WHERE id = ?";

        int rows = executeUpdate(sql, id);

        return rows > 0;
    }

    public boolean addStock(int productId, int qty,
                            Integer supplierId, Integer createdBy) {

        String sql = "INSERT INTO stock " +
                "(qty, created_by, product_id, supplier_id) " +
                "VALUES (?, ?, ?, ?)";

        int rows = executeUpdate(sql, qty, createdBy,
                productId, supplierId);

        return rows > 0;
    }

    public List<Product> searchProducts(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }

        String sql = "SELECT p.*, c.name AS category_name, " +
                "u.name AS created_by_name, " +
                "(SELECT SUM(qty) FROM stock WHERE stock.product_id = p.id) AS quantity " +
                "FROM product p " +
                "LEFT JOIN product_category c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.created_by = u.id " +
                "WHERE p.name ILIKE ? " +
                "OR c.name ILIKE ? " +
                "OR u.name ILIKE ? " +
                "OR p.barcode ILIKE ? " +
                "ORDER BY p.id";

        return loadProducts(sql, "%" + keyword.trim() + "%");
    }

    private List<Product> loadProducts(String sql, String keyword) {

        List<Product> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (keyword != null) {
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ps.setString(3, keyword);
                ps.setString(4, keyword);
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Product product = Product.builder()
                            .id(rs.getInt("id"))
                            .name(rs.getString("name"))
                            .categoryId((Integer) rs.getObject("category_id"))
                            .price(rs.getDouble("price"))
                            .sellPrice(rs.getDouble("sell_price"))
                            .createdBy((Integer) rs.getObject("created_by"))
                            .categoryName(rs.getString("category_name"))
                            .createdByName(rs.getString("created_by_name"))
                            .quantity(rs.getInt("quantity"))
                            .barcode(rs.getString("barcode"))
                            .build();

                    list.add(product);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public Map<Integer, String> getAllCategories() {

        Map<Integer, String> categories = new LinkedHashMap<>();

        String sql = "SELECT id, name FROM product_category ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.put(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return categories;
    }

    public Map<Integer, String> getAllUsers() {

        Map<Integer, String> users = new LinkedHashMap<>();

        String sql = "SELECT id, name FROM users ORDER BY name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.put(
                        rs.getInt("id"),
                        rs.getString("name")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }
}

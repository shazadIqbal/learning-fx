
package com.mls.learningfx.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedHashMap;
import java.util.Map;

public class SaleReportRepository extends BaseRepository {


    public Map<String, Integer> getLowStockProducts() {

        String sql = "SELECT p.id, p.name, " +
                "SUM(st.qty) AS total_stock " +
                "FROM product p " +
                "JOIN stock st ON p.id = st.product_id " +
                "GROUP BY p.id, p.name " +
                "HAVING SUM(st.qty) <= 30 " +
                "ORDER BY total_stock ASC";

        Map<String, Integer> result = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String productName =
                        rs.getString("name");


                int quantity =
                        rs.getInt("total_stock");

                result.put(
                        productName,
                        quantity
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch low stock products", e
            );
        }

        return result;
    }


    public Map<String, Double> getDayWiseSales() {

        String sql = "SELECT DATE(date) AS sale_day, " +
                        "SUM(total_price) AS total_sales " +
                        "FROM sale " +
                        "GROUP BY DATE(date) " +
                        "ORDER BY DATE(date)";

        Map<String, Double> result = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String saleDate =
                        rs.getString("sale_day");

                double totalSales =
                        rs.getDouble("total_sales");

                result.put(saleDate, totalSales);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch day-wise sales", e
            );
        }

        return result;
    }


    public Map<String, Double> getDayWiseProfit() {

        String sql = "SELECT DATE(s.date) AS sale_day, " +
                        "SUM((p.sell_price - p.price) * si.qty) AS total_profit " +
                        "FROM sale s " +
                        "JOIN sale_item si ON s.s_id = si.s_id " +
                        "JOIN product p ON si.p_id = p.id " +
                        "GROUP BY DATE(s.date) " +
                        "ORDER BY DATE(s.date)";

        Map<String, Double> result = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String saleDate =
                        rs.getString("sale_day");

                double totalProfit =
                        rs.getDouble("total_profit");

                result.put(saleDate, totalProfit);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch day-wise profit", e
            );
        }

        return result;
    }


    public Map<String, Integer> getMostSellingProducts() {

        String sql = "SELECT p.id, p.name, " +
                        "SUM(si.qty) AS total_sold " +
                        "FROM sale_item si " +
                        "JOIN product p ON si.p_id = p.id " +
                        "GROUP BY p.id, p.name " +
                        "ORDER BY total_sold DESC, p.id ASC ";

        Map<String, Integer> result = new LinkedHashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                String productName =
                        rs.getString("name");


                int totalSold =
                        rs.getInt("total_sold");

                result.put(
                        productName,
                        totalSold
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to fetch most-selling products", e
            );
        }

        return result;
    }
}

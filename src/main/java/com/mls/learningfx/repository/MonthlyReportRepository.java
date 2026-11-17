package com.mls.learningfx.repository;

import com.mls.learningfx.model.MonthlySale;
import com.mls.learningfx.model.ProductSale;
import com.mls.learningfx.model.SaleDetail;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportRepository extends BaseRepository {



    public List<MonthlySale> findMonthlySales(YearMonth from, YearMonth to) throws SQLException {

        String sql = "SELECT EXTRACT(YEAR FROM s.date) AS sale_year, "
                + "EXTRACT(MONTH FROM s.date) AS sale_month, "
                + "SUM(si.qty) AS qty, "
                + "SUM(si.qty * p.sell_price) AS total, "
                + "SUM((p.sell_price - p.price) * si.qty) AS profit "
                + "FROM sale s "
                + "JOIN sale_item si ON s.s_id = si.s_id "
                + "JOIN product p ON si.p_id = p.id "
                + "WHERE s.date >= ? AND s.date < ? "
                + "GROUP BY sale_year, sale_month "
                + "ORDER BY sale_year, sale_month";

        List<MonthlySale> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from.atDay(1)));
            stmt.setDate(2, Date.valueOf(to.plusMonths(1).atDay(1)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    int year = rs.getInt("sale_year");
                    int month = rs.getInt("sale_month");

                    rows.add(new MonthlySale(
                            LocalDate.of(year, month, 1),
                            rs.getInt("qty"),
                            rs.getDouble("total"),
                            rs.getDouble("profit")));
                }
            }
        }
        return rows;
    }
    public List<ProductSale> findProductSales(YearMonth from, YearMonth to) throws SQLException {

        String sql = "SELECT p.name, p.price AS base_price, p.sell_price, "
                + "SUM(si.qty) AS qty, "
                + "SUM(si.qty * p.sell_price) AS total, "
                + "SUM((p.sell_price - p.price) * si.qty) AS profit "
                + "FROM sale s "
                + "JOIN sale_item si ON s.s_id = si.s_id "
                + "JOIN product p ON si.p_id = p.id "
                + "WHERE s.date >= ? AND s.date < ? "
                + "GROUP BY p.id, p.name, p.price, p.sell_price "
                + "ORDER BY SUM(si.qty) DESC";

        List<ProductSale> rows = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from.atDay(1)));
            stmt.setDate(2, Date.valueOf(to.plusMonths(1).atDay(1)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ProductSale(
                            rs.getString("name"),
                            rs.getDouble("base_price"),
                            rs.getDouble("sell_price"),
                            rs.getInt("qty"),
                            rs.getDouble("total"),
                            rs.getDouble("profit")));
                }
            }
        }
        return rows;
    }

    // Har sale ki date ke saath detail
    public List<SaleDetail> findSaleDetails(YearMonth from, YearMonth to) throws SQLException {

        String sql = "SELECT s.date AS sale_date, p.name, si.qty, "
                + "(si.qty * p.sell_price) AS total, "
                + "((p.sell_price - p.price) * si.qty) AS profit "
                + "FROM sale s "
                + "JOIN sale_item si ON s.s_id = si.s_id "
                + "JOIN product p ON si.p_id = p.id "
                + "WHERE s.date >= ? AND s.date < ? "
                + "ORDER BY s.date, p.name";

        List<SaleDetail> list = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from.atDay(1)));
            stmt.setDate(2, Date.valueOf(to.plusMonths(1).atDay(1)));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SaleDetail(
                            rs.getDate("sale_date").toLocalDate(),
                            rs.getString("name"),
                            rs.getInt("qty"),
                            rs.getDouble("total"),
                            rs.getDouble("profit")));
                }
            }
        }
        return list;
    }
}
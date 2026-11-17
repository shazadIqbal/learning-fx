package com.mls.learningfx.repository;

import com.mls.learningfx.model.Expense;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository extends BaseRepository {

    // READ ALL EXPENSES
    public List<Expense> getAllExpense() {
        return searchExpense("");
    }

    // SEARCH EXPENSE BY DETAIL
    public List<Expense> searchExpense(String keyword) {

        List<Expense> expenseList = new ArrayList<>();

        String sql = "SELECT * FROM expense " +
                "WHERE detail ILIKE ? " +
                "ORDER BY expense_date DESC, id DESC";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                expenseList.add(new Expense(
                        rs.getInt("id"),
                        rs.getString("detail"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("expense_date").toLocalDate()
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return expenseList;
    }

    // CREATE EXPENSE
    public boolean addExpense(String detail, BigDecimal amount, LocalDate date) {

        String sql = "INSERT INTO expense(detail, amount, expense_date) VALUES(?, ?, ?)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, detail);
            ps.setBigDecimal(2, amount);
            ps.setDate(3, Date.valueOf(date));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE EXPENSE
    public boolean updateExpenseById(
            Integer id,
            String detail,
            BigDecimal amount,
            LocalDate date) {

        String sql = "UPDATE expense SET detail = ?, amount = ?, expense_date = ? WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setString(1, detail);
            ps.setBigDecimal(2, amount);
            ps.setDate(3, Date.valueOf(date));
            ps.setInt(4, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // DELETE EXPENSE
    public boolean deleteExpenseById(Integer id) {

        String sql = "DELETE FROM expense WHERE id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // MONTHLY TOTAL (for monthly reports)
    public BigDecimal getMonthlyTotal(int year, int month) {

        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expense " +
                "WHERE EXTRACT(YEAR FROM expense_date) = ? " +
                "AND EXTRACT(MONTH FROM expense_date) = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {

            ps.setInt(1, year);
            ps.setInt(2, month);

            ResultSet rs = ps.executeQuery();

            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
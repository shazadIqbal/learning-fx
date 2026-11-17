package com.mls.learningfx.service;

import com.mls.learningfx.model.Expense;
import com.mls.learningfx.repository.ExpenseRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseService {

    private final ExpenseRepository expenseRepository =
            new ExpenseRepository();

    // CREATE
    public boolean addExpense(String detail, BigDecimal amount, LocalDate date) {

        return expenseRepository.addExpense(detail, amount, date);
    }

    // READ ALL
    public ObservableList<Expense> getAllExpenses() {

        return FXCollections.observableArrayList(
                expenseRepository.getAllExpense()
        );
    }

    // SEARCH
    public ObservableList<Expense> searchExpense(String keyword) {

        return FXCollections.observableArrayList(
                expenseRepository.searchExpense(keyword)
        );
    }

    // UPDATE
    public boolean updateExpense(Integer id, String detail,
                                 BigDecimal amount, LocalDate date) {

        return expenseRepository.updateExpenseById(id, detail, amount, date);
    }

    // DELETE
    public boolean deleteExpense(Integer id) {

        return expenseRepository.deleteExpenseById(id);
    }

    // MONTHLY REPORT
    public BigDecimal getMonthlyTotal(int year, int month) {

        return expenseRepository.getMonthlyTotal(year, month);
    }
}
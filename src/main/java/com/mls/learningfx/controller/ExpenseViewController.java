package com.mls.learningfx.controller;

import com.mls.learningfx.model.Expense;
import com.mls.learningfx.service.ExpenseService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class ExpenseViewController {

    @FXML private TextField searchField;
    @FXML private TableView<Expense> tableView;
    @FXML private TableColumn<Expense, Integer> idColumn;
    @FXML private TableColumn<Expense, String> detailColumn;
    @FXML private TableColumn<Expense, BigDecimal> amountColumn;
    @FXML private TableColumn<Expense, LocalDate> dateColumn;
    @FXML private TableColumn<Expense, Void> actionColumn;
    @FXML private Button backButton;

    private final ExpenseService expenseService = new ExpenseService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        detailColumn.setCellValueFactory(new PropertyValueFactory<>("detail"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        addButton();
        getAllExpenses();

        searchField.setOnAction(e -> handleSearch());
        backButton.setOnAction(e -> backToDashboard());
    }

    private void backToDashboard() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        try {
            NavigationUtil.navigate(stage, NavigationUtil.DASHBOARD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------- CREATE ----------
    @FXML
    public void addExpense() {
        Optional<Expense> result = showExpenseDialog("Add Expense", "Save", null);

        result.ifPresent(exp -> {
            boolean added = expenseService.addExpense(
                    exp.getDetail(), exp.getAmount(), exp.getDate());
            if (added) {
                getAllExpenses();
                showSuccess("Expense Added Successfully!");
            } else {
                showError("Failed to Add Expense");
            }
        });
    }

    // ---------- READ ----------
    @FXML
    public void getAllExpenses() {
        tableView.setItems(expenseService.getAllExpenses());
    }

    @FXML
    public void handleSearch() {
        ObservableList<Expense> result =
                expenseService.searchExpense(searchField.getText().trim());
        tableView.setItems(result);
        if (result.isEmpty()) showError("No Expense Found!");
    }

    @FXML
    public void handleShowAll() {
        searchField.clear();
        getAllExpenses();
    }

    // ---------- ACTION COLUMN ----------
    private void addButton() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox hBox = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> {
                    Expense exp = getTableRow().getItem();
                    if (exp != null) editExpense(exp);
                });
                deleteBtn.setOnAction(e -> {
                    Expense exp = getTableRow().getItem();
                    if (exp != null) deleteExpense(exp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hBox);
            }
        });
    }

    // ---------- UPDATE ----------
    private void editExpense(Expense expense) {
        Optional<Expense> result = showExpenseDialog("Edit Expense", "Update", expense);

        result.ifPresent(exp -> {
            boolean updated = expenseService.updateExpense(
                    exp.getId(), exp.getDetail(), exp.getAmount(), exp.getDate());
            if (updated) {
                handleCurrentSearch();
                showSuccess("Expense Successfully Updated!");
            } else {
                showError("Failed to Update Expense");
            }
        });
    }

    // ---------- DELETE ----------
    private void deleteExpense(Expense expense) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Expense");
        confirmation.setHeaderText("Confirm Expense Deletion");
        confirmation.setContentText("Delete expense \"" + expense.getDetail() + "\"?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (expenseService.deleteExpense(expense.getId())) {
                handleCurrentSearch();
                showSuccess("Expense Successfully Deleted!");
            } else {
                showError("Failed to Delete Expense");
            }
        }
    }

    // ---------- SHARED ADD/EDIT DIALOG ----------
    // existing == null  -> add mode, otherwise edit mode
    private Optional<Expense> showExpenseDialog(String title, String actionText, Expense existing) {

        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Enter Expense Details");

        ButtonType actionButton = new ButtonType(actionText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(actionButton, ButtonType.CANCEL);

        TextField detailField = new TextField(existing != null ? existing.getDetail() : "");
        detailField.setPromptText("e.g. Shop rent, electricity bill");

        TextField amountField = new TextField(
                existing != null ? existing.getAmount().toPlainString() : "");
        amountField.setPromptText("Amount");

        DatePicker datePicker = new DatePicker(
                existing != null ? existing.getDate() : LocalDate.now());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.add(new Label("Detail:"), 0, 0);
        grid.add(detailField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Block the dialog from closing when input is invalid
        dialog.getDialogPane().lookupButton(actionButton).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    if (!validateExpense(detailField.getText(),
                            amountField.getText(), datePicker.getValue())) {
                        event.consume();
                    }
                });

        dialog.setResultConverter(button -> {
            if (button == actionButton) {
                return new Expense(
                        existing != null ? existing.getId() : null,
                        detailField.getText().trim(),
                        new BigDecimal(amountField.getText().trim()),
                        datePicker.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void handleCurrentSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) getAllExpenses();
        else tableView.setItems(expenseService.searchExpense(keyword));
    }

    // ---------- VALIDATION ----------
    private boolean validateExpense(String detail, String amountText, LocalDate date) {
        if (detail == null || detail.trim().isEmpty()) {
            showError("Expense Detail Cannot Be Empty!");
            return false;
        }
        try {
            if (new BigDecimal(amountText.trim()).compareTo(BigDecimal.ZERO) <= 0) {
                showError("Amount Must Be Greater Than 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Enter a Valid Amount!");
            return false;
        }
        if (date == null) {
            showError("Please Select a Date!");
            return false;
        }
        return true;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
package com.mls.learningfx.controller;

import com.mls.learningfx.model.Role;
import com.mls.learningfx.model.User;
import com.mls.learningfx.service.UserService;
import com.mls.learningfx.util.NavigationUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Long> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> contactColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> passwordColumn;

    @FXML
    private TableColumn<User, Role> roleColumn;

    @FXML
    private TableColumn<User, User> actionColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button addUserButton;

    private final UserService userService = new UserService();


    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        contactColumn.setCellValueFactory(
                new PropertyValueFactory<>("contact")
        );

        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("email")
        );

        passwordColumn.setCellValueFactory(
                new PropertyValueFactory<>("password")
        );

        roleColumn.setCellValueFactory(
                new PropertyValueFactory<>("role")
        );

        addEditDeleteButtons();

        getAllUsers();

        addUserButton.setOnAction(e -> addUser());

        backButton.setOnAction(e -> backToDashboard());
    }


    public void getAllUsers() {

        userTable.setItems(
                FXCollections.observableArrayList(userService.getAllUsers())
        );
    }


    private void addEditDeleteButtons() {

        actionColumn.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(
                        cellData.getValue()
                )
        );

        actionColumn.setCellFactory(col -> new TableCell<User, User>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            private final HBox box = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> {

                    User user = getItem();

                    if (user != null) {
                        editUser(user);
                    }
                });

                deleteBtn.setOnAction(e -> {

                    User user = getItem();

                    if (user != null) {
                        deleteUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(User user, boolean empty) {

                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }


    private void addUser() {
        showUserDialog(null);
    }


    private void editUser(User user) {
        showUserDialog(user);
    }


    // same dialog for add and edit, user is null when adding
    private void showUserDialog(User user) {

        boolean isEdit = user != null;

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField contactField = new TextField();
        contactField.setPromptText("Contact");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");

        ComboBox<Role> roleBox = new ComboBox<>(
                FXCollections.observableArrayList(Role.values())
        );
        roleBox.setPromptText("Select role");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        // fill old values when editing
        if (isEdit) {
            nameField.setText(user.getName());
            contactField.setText(user.getContact());
            emailField.setText(user.getEmail());
            passwordField.setText(user.getPassword());
            roleBox.setValue(user.getRole());
        }

        VBox vbox = new VBox(10);

        vbox.setPadding(new Insets(20));
        vbox.setPrefWidth(320);

        vbox.getChildren().addAll(
                new Label("Name"),
                nameField,

                new Label("Contact"),
                contactField,

                new Label("Email"),
                emailField,

                new Label("Password"),
                passwordField,

                new Label("Role"),
                roleBox
        );

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(isEdit ? "Edit User" : "Add User");
        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButton = new ButtonType(
                "Save",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            User newUser = User.builder()
                    .id(isEdit ? user.getId() : null)
                    .name(nameField.getText().trim())
                    .contact(contactField.getText().trim())
                    .email(emailField.getText().trim())
                    .password(passwordField.getText().trim())
                    .role(roleBox.getValue())
                    .build();

            try {

                boolean saved = isEdit
                        ? userService.updateUser(newUser)
                        : userService.addUser(newUser);

                if (saved) {

                    showSuccess(
                            isEdit
                                    ? "User updated successfully"
                                    : "User added successfully"
                    );

                    getAllUsers();

                } else {

                    showError("Please fill name, email, password and role");
                }

            } catch (RuntimeException e) {

                showError("Could not save user. This email may already be used.");
            }
        }
    }


    private void deleteUser(User user) {

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Delete User");
        confirmation.setHeaderText(
                "Delete " + user.getName() + "?"
        );

        confirmation.setContentText(
                "Are you sure you want to delete this user?"
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            try {

                boolean deleted =
                        userService.deleteUser(user.getId().intValue());

                if (deleted) {

                    showSuccess("User deleted successfully");
                    getAllUsers();

                } else {

                    showError("User could not be deleted. You cannot delete your own account.");
                }

            } catch (RuntimeException e) {

                showError("Could not delete this user: " + e.getMessage());
            }
        }
    }


    private void showSuccess(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void backToDashboard() {

        Stage stage = (Stage)
                backButton.getScene().getWindow();

        try {

            NavigationUtil.navigate(
                    stage,
                    NavigationUtil.DASHBOARD
            );

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }
}

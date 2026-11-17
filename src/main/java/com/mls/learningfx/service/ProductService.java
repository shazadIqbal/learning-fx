package com.mls.learningfx.service;

import com.mls.learningfx.model.Product;
import com.mls.learningfx.repository.ProductRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductService {

    private final ProductRepository productRepository = new ProductRepository();

    public ObservableList<Product> getAllProducts() {

        ObservableList<Product> observableList =
                FXCollections.observableArrayList();

        List<Product> list = productRepository.getAllProducts();

        if (list.isEmpty()) {
            System.out.println("No products found");
        }

        observableList.addAll(list);

        return observableList;
    }

    public Integer addProduct(String name, Integer categoryId,
                              Double price, Double sellPrice,
                              Integer createdBy, String barcode,
                              LocalDate expiryDate) {

        return productRepository.addProduct(
                name, categoryId, price, sellPrice, createdBy, barcode, expiryDate
        );
    }

    public boolean updateProduct(int id, String name,
                                 Integer categoryId, Double price,
                                 Double sellPrice, String barcode,
                                 LocalDate expiryDate) {

        return productRepository.updateProduct(
                id, name, categoryId, price, sellPrice, barcode, expiryDate
        );
    }

    public boolean deleteProduct(int id) {

        return productRepository.deleteProduct(id);
    }

    public boolean addStock(int productId, int qty,
                            Integer supplierId, Integer createdBy) {

        return productRepository.addStock(
                productId, qty, supplierId, createdBy
        );
    }

    public ObservableList<Product> searchProducts(String keyword) {

        ObservableList<Product> observableList =
                FXCollections.observableArrayList();

        List<Product> list = productRepository.searchProducts(keyword);

        observableList.addAll(list);

        return observableList;
    }

    public Map<Integer, String> getAllCategories() {

        return productRepository.getAllCategories();
    }

    public Map<Integer, String> getAllUsers() {

        return productRepository.getAllUsers();
    }

    public ObservableList<Product> getProductsWithExpiry() {

        ObservableList<Product> observableList =
                FXCollections.observableArrayList();

        observableList.addAll(productRepository.getProductsWithExpiry());

        return observableList;
    }

    public ObservableList<Product> getExpiredProducts() {

        LocalDate today = LocalDate.now();
        ObservableList<Product> expired = FXCollections.observableArrayList();

        for (Product p : productRepository.getProductsWithExpiry()) {
            if (p.getExpiryDate() != null && p.getExpiryDate().isBefore(today)) {
                expired.add(p);
            }
        }

        return expired;
    }

    public ObservableList<Product> getNearExpiryProducts(int withinDays) {

        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(withinDays);
        ObservableList<Product> nearExpiry = FXCollections.observableArrayList();

        for (Product p : productRepository.getProductsWithExpiry()) {
            if (p.getExpiryDate() != null
                    && !p.getExpiryDate().isBefore(today)
                    && !p.getExpiryDate().isAfter(limit)) {
                nearExpiry.add(p);
            }
        }

        return nearExpiry;
    }

    public List<String> getExpiryNotifications(int withinDays) {

        List<String> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(withinDays);

        for (Product p : productRepository.getProductsWithExpiry()) {
            LocalDate exp = p.getExpiryDate();
            if (exp == null) {
                continue;
            }
            if (exp.isBefore(today)) {
                long daysAgo = ChronoUnit.DAYS.between(exp, today);
                notifications.add("EXPIRED: " + p.getName()
                        + " (Expired on " + exp + " - " + daysAgo + " days ago, Stock: " + p.getQuantity() + ")");
            } else if (!exp.isAfter(limit)) {
                long daysLeft = ChronoUnit.DAYS.between(today, exp);
                notifications.add("EXPIRING SOON: " + p.getName()
                        + " (Expires on " + exp + " - " + daysLeft + " days left, Stock: " + p.getQuantity() + ")");
            }
        }

        return notifications;
    }
}


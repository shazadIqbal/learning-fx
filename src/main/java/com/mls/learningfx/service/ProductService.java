
package com.mls.learningfx.service;

import com.mls.learningfx.model.Product;
import com.mls.learningfx.repository.ProductRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
                              Integer createdBy, String barcode) {

        return productRepository.addProduct(
                name, categoryId, price, sellPrice, createdBy, barcode
        );
    }

    public boolean updateProduct(int id, String name,
                                 Integer categoryId, Double price,
                                 Double sellPrice, String barcode) {

        return productRepository.updateProduct(
                id, name, categoryId, price, sellPrice, barcode
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
}

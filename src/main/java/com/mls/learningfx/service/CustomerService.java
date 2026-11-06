
package com.mls.learningfx.service;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.repository.CustomerRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepository =
            new CustomerRepository();

    // CREATE
    public boolean addCustomer(String name, String contact) {

        return customerRepository.addCustomer(name, contact);
    }

    // READ ALL
    public ObservableList<Customer> getAllCustomer() {

        ObservableList<Customer> observableList =
                FXCollections.observableArrayList();

        List<Customer> customers =
                customerRepository.getAllCustomer();

        if (customers.isEmpty()) {
            System.out.println("Nothing 2 C here");
        }

        observableList.addAll(customers);

        return observableList;
    }

    // SEARCH
    public ObservableList<Customer> searchCustomer(String keyword) {

        ObservableList<Customer> observableList =
                FXCollections.observableArrayList();

        List<Customer> customers =
                customerRepository.searchCustomer(keyword);

        observableList.addAll(customers);

        return observableList;
    }

    // UPDATE
    public boolean updateCustomer(
            Integer id,
            String name,
            String contact) {

        return customerRepository.updateCustomerById(
                id, name, contact
        );
    }

    // DELETE
    public boolean deleteCustomer(Integer id) {

        return customerRepository.deleteCustomerById(id);
    }
}

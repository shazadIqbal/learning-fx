
package com.mls.learningfx.repository;

import com.mls.learningfx.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository extends BaseRepository {

    // READ ALL CUSTOMERS
    public List<Customer> getAllCustomer() {
        return searchCustomer("");
    }

    // SEARCH CUSTOMER BY NAME OR CONTACT
    public List<Customer> searchCustomer(String keyword) {

        List<Customer> customerList = new ArrayList<>();

        String sql = "SELECT * FROM customer " +
                "WHERE name ILIKE ? OR contact ILIKE ? " +
                "ORDER BY id";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customerList.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return customerList;
    }

    // CREATE CUSTOMER
    public boolean addCustomer(String name, String contact) {

        String sql = "INSERT INTO customer(name, contact) VALUES(?, ?)";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, contact);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // UPDATE CUSTOMER
    public boolean updateCustomerById(
            Integer id,
            String name,
            String contact) {

        String sql = "UPDATE customer SET name = ?, contact = ? WHERE id = ?";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, contact);
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // DELETE CUSTOMER
    public boolean deleteCustomerById(Integer id) {

        String sql = "DELETE FROM customer WHERE id = ?";

        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

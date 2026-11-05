package com.mls.learningfx.repository;

import com.mls.learningfx.model.Supplier;

import com.mls.learningfx.model.SupplierType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository extends BaseRepository{
    public List<Supplier> getAllSuppliers() {

        List<Supplier> supplierList = new ArrayList<>();

        String sql = "SELECT s.id, s.name, s.contact, s.type, " +
                "st.name AS supplier_type, " +
                "s.company_name " +
                "FROM supplier s " +
                "INNER JOIN supplier_type st ON s.type = st.id";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Supplier supplier = new Supplier();

                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContact(rs.getString("contact"));
                supplier.setTypeId(rs.getInt("type"));

                supplier.setType(rs.getString("supplier_type"));
                supplier.setCompanyName(rs.getString("company_name"));

                supplierList.add(supplier);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return supplierList;
    }


    public boolean addSupplier(Supplier supplier) {

        String sql = "INSERT INTO supplier " +
                "(name, contact, type, company_name) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContact());
            ps.setInt(3, supplier.getTypeId());
            ps.setString(4, supplier.getCompanyName());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSupplier(Supplier supplier) {
        String sql = "UPDATE supplier SET name = ?, contact = ?, type = ?, " +
                "company_name = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getContact());
            ps.setInt(3, supplier.getTypeId());
            ps.setString(4, supplier.getCompanyName());
            ps.setInt(5, supplier.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean deleteSupplier(Integer id) {
        String sql = "delete from supplier where id = ?";
        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1,id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<SupplierType> getAllSupplierTypes() {

        List<SupplierType> types = new ArrayList<>();

        String sql = "SELECT id, name FROM supplier_type ORDER BY id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                SupplierType type = new SupplierType();

                type.setId(rs.getInt("id"));
                type.setName(rs.getString("name"));

                types.add(type);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return types;
    }


}

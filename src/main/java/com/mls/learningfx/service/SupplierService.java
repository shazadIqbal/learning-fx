package com.mls.learningfx.service;

import com.mls.learningfx.model.Supplier;
import com.mls.learningfx.model.SupplierType;
import com.mls.learningfx.repository.SupplierRepository;

import java.util.List;

public class SupplierService {
    private final SupplierRepository supplierRepository = new SupplierRepository();

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.getAllSuppliers();
    }

    public boolean addSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            return false;
        }

        return supplierRepository.addSupplier(supplier);
    }

    public boolean updateSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank()
                || supplier.getContact() == null || supplier.getContact().isBlank()
                || supplier.getCompanyName() == null || supplier.getCompanyName().isBlank()
                || supplier.getTypeId() <= 0) {
            return false;
        }
        return supplierRepository.updateSupplier(supplier);
    }

    public boolean deleteSupplier(Integer id){
        return supplierRepository.deleteSupplier(id);
    }


    public List<SupplierType> getAllSupplierTypes() {
        return supplierRepository.getAllSupplierTypes();
    }

}

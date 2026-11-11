package com.mls.learningfx.service;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;
import com.mls.learningfx.repository.SaleRepository;

import java.util.List;

public class SaleService {

    private final SaleRepository saleRepository = new SaleRepository();

    public Integer findCustomerIdByPhone(String phone) {
        return saleRepository.findCustomerIdByPhone(phone);
    }

    public int checkout(Integer customerId,
                        Customer newCustomer,
                        Integer userId,
                        double totalPrice,
                        String paymentMode,
                        List<Item> items) {

        return saleRepository.checkout(
                customerId,
                newCustomer,
                userId,
                totalPrice,
                paymentMode,
                items
        );
    }
}

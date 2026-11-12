package com.mls.learningfx.service;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;
import com.mls.learningfx.repository.SaleRepository;
import com.mls.learningfx.model.CustomerSaleHistory;
import java.math.BigDecimal;
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

    // CUSTOMER SALE HISTORY
    public List<CustomerSaleHistory> getCustomerSaleHistory(
            Integer customerId) {

        return saleRepository.getCustomerSaleHistory(customerId);
    }

    // CUSTOMER TOTAL SPENDINGS
    public BigDecimal getCustomerTotalSpendings(
            Integer customerId) {

        return saleRepository.getCustomerTotalSpendings(customerId);
    }

}

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

    // Items of one sale
    public List<Item> getSaleItems(int saleId) {

        return saleRepository.getSaleItems(saleId);
    }

    // Checks return qty - gives an error message, or null if all ok
    public String validateReturns(List<Item> soldItems,
                                  List<Integer> returnQtys) {

        int totalReturned = 0;

        for (int i = 0; i < soldItems.size(); i++) {

            Item item = soldItems.get(i);
            int returnQty = returnQtys.get(i);

            if (returnQty < 0 || returnQty > item.getQuantity()) {
                return "Return qty for " + item.getName()
                        + " must be between 0 and " + item.getQuantity() + ".";
            }

            totalReturned += returnQty;
        }

        if (totalReturned == 0) {
            return "No items were returned, so nothing was changed.";
        }

        return null;
    }

    // Calculating refund amount
    public double calculateRefund(List<Item> soldItems,
                                  List<Integer> returnQtys) {

        double refundAmount = 0;

        for (int i = 0; i < soldItems.size(); i++) {
            refundAmount += returnQtys.get(i) * soldItems.get(i).getPrice();
        }

        return refundAmount;
    }

    // RETURN ITEMS FROM A SALE
    public void returnItems(int saleId,
                            List<Item> soldItems,
                            List<Integer> returnQtys,
                            Integer userId) {

        String error = validateReturns(soldItems, returnQtys);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        double refundAmount = calculateRefund(soldItems, returnQtys);

        saleRepository.returnItems(
                saleId,
                soldItems,
                returnQtys,
                refundAmount,
                userId
        );
    }

}

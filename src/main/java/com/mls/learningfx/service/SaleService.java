package com.mls.learningfx.service;

import com.mls.learningfx.model.Customer;
import com.mls.learningfx.model.Item;
import com.mls.learningfx.repository.SaleRepository;
import com.mls.learningfx.model.CustomerSaleHistory;
import com.mls.learningfx.model.SaleRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    // GET SALE RECORD BY SALE ID
    public SaleRecord getSaleRecordById(int saleId) {
        return saleRepository.getSaleRecordById(saleId);
    }

    // UPDATE ITEMS OF AN OLD SALE
    public void updateSale(int saleId, double totalPrice, List<Item> items) {

        List<Item> oldItems = saleRepository.getSaleItems(saleId);

        // only the difference goes in or out of stock
        List<Item> soldMore = new ArrayList<>();
        List<Item> returned = new ArrayList<>();

        // items added or qty changed
        for (Item newItem : items) {
            int change = newItem.getQuantity() - getQty(oldItems, newItem.getProductId());

            if (change > 0) {
                soldMore.add(copyWithQty(newItem, change));
            } else if (change < 0) {
                returned.add(copyWithQty(newItem, -change));
            }
        }

        // items removed from the sale
        for (Item oldItem : oldItems) {
            if (getQty(items, oldItem.getProductId()) == 0) {
                returned.add(oldItem);
            }
        }

        saleRepository.updateSale(saleId, totalPrice, items, soldMore, returned);
    }

    // qty of a product in a list, 0 if not there
    private int getQty(List<Item> items, Integer productId) {
        for (Item item : items) {
            if (item.getProductId().equals(productId)) {
                return item.getQuantity();
            }
        }
        return 0;
    }

    private Item copyWithQty(Item item, int qty) {
        return Item.builder()
                .productId(item.getProductId())
                .name(item.getName())
                .quantity(qty)
                .build();
    }

}


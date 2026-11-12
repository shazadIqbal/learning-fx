package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class CustomerSaleHistory {

        private Integer saleId;
        private String purchaseDate;
        private String paymentMode;
        private BigDecimal totalPrice;
        private String purchasedItems;
    }



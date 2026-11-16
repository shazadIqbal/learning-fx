package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleRecord {

    private Integer saleId;
    private String saleDate;
    private String paymentMode;
    private BigDecimal totalPrice;
    private Customer customer;
    private String soldBy;
    private List<Item> items;
}


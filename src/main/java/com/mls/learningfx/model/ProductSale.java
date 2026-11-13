package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSale {

    private final String name;
    private final double basePrice;
    private final double sellPrice;
    private final int totalQty;
    private final double totalSales;
    private final double totalProfit;
}
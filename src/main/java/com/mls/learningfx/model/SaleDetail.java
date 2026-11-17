package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SaleDetail {

    private final LocalDate date;
    private final String productName;
    private final int qty;
    private final double sales;
    private final double profit;
}
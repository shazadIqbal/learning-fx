package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class MonthlySale {

    private final LocalDate day;
    private final int totalQty;
    private final double totalSales;
    private final double totalProfit;
}
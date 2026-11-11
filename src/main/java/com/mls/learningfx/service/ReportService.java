package com.mls.learningfx.service;

import com.mls.learningfx.repository.SaleReportRepository;

import java.util.Map;

public class ReportService {

    private final SaleReportRepository saleReportRepository =  new SaleReportRepository();


    public Map<String, Integer> getLowStockProducts() {

        return saleReportRepository.getLowStockProducts();
    }


    public Map<String, Double> getDayWiseSales() {

        return saleReportRepository.getDayWiseSales();
    }


    public Map<String, Double> getDayWiseProfit() {

        return saleReportRepository.getDayWiseProfit();
    }


    public Map<String, Integer> getMostSellingProducts() {

        return saleReportRepository.getMostSellingProducts();
    }
}

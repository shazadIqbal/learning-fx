package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private int id;
    private String name;
    private Integer categoryId;
    private Double price;
    private Double sellPrice;
    private Integer createdBy;

    private String categoryName;
    private String createdByName;
    private Integer quantity;
    private String barcode;
    private LocalDate expiryDate;

}
package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    private String name;
    private Integer quantity;
    private Double price;
    private Double subTotal;
    private Integer productId;


}

package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Expense {
    private Integer id;
    private String detail;
    private BigDecimal amount;
    private LocalDate date;


}
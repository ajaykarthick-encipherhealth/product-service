package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class BudgetDTO {
    private Long minBudget;
    private Long maxBudget;
    private String currency;
}
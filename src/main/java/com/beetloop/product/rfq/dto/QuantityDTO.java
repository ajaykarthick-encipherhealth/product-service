package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class QuantityDTO {
    private Integer quantityRequired;
    private String unit;
}
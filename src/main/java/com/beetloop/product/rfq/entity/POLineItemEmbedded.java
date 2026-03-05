package com.beetloop.product.rfq.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class POLineItemEmbedded {
    private String itemDescription;
    private String specificationOrSku;
    private String quantity;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal taxPercent;
    private BigDecimal lineTotal;
}

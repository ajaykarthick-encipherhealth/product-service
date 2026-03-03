package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class VolumeBasedPricing {
    private String quantityFrom;
    private String quantityTo;
    private String discount;
}

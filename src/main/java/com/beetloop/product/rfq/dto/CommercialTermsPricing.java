package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommercialTermsPricing {
    private String paymentTerms;
    private String quoteValidityPeriod;
    private String deliveryTerms;
    private String currency;
    private List<VolumeBasedPricing> volumeBasedPricings;
    private String additionalTerms;
}

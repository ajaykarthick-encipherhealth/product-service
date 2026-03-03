package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class CommercialTermsDTO {
    private QuantityDTO quantity;
    private QuantityDTO maxAcceptableMoq;
    private String orderFrequency;
    private String contractDuration;
    private BudgetDTO shareBudgetRange;
    private String deliveryLocation;
    private String incoterms;
    private String paymentTerms;
    private String packagingRequirement;
    private Boolean hasNeedSample;
    private String sampleType;
    private QuantityDTO expectedQuantity;
    private String preferredTimeLine;
}

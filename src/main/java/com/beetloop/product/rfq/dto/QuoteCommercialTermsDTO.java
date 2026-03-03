package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class QuoteCommercialTermsDTO {
    private CommercialTermsPricing commercialTermsPricing;
    private SLACommitment slaCommitment;
}


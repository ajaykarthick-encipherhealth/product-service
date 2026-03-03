package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class QuoteRequestDTO {

    private QuoteCommercialTermsDTO quoteCommercialTermsDTO;
    private QuoteDetails quoteInfo;
    private String notes;
}

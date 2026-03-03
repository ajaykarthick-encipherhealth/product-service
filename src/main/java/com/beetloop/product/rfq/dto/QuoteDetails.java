package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuoteDetails {
    private List<QuoteSections> quoteSections;
}

package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuoteFields {
    private String key;
    private String label;
    private String type;
    private Object value;
    private List<String> options;
    private Boolean required;
}

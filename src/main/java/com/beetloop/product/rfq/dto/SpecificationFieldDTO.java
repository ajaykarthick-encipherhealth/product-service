package com.beetloop.product.rfq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationFieldDTO {

    private String key;

    private String label;

    private String type;

    private Object value;

    private List<String> options;

    private Boolean required;
}

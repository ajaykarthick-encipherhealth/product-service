package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class PODocumentRequirementDTO {
    private String documentName;
    private String requiredFor;
    private boolean mandatory;
    private String status;
}

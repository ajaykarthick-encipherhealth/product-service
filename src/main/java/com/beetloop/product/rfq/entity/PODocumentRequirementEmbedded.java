package com.beetloop.product.rfq.entity;

import lombok.Data;

@Data
public class PODocumentRequirementEmbedded {
    private String documentName;
    private String requiredFor;  // e.g. "Customs", "Logistics"
    private boolean mandatory;
    private String status;       // PENDING, UPLOADED, NOT_APPLICABLE
}

package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.RfqStatus;
import lombok.Data;

@Data
public class LeadResponseDTO {

    private String id;
    private String buyerId;
    private RfqStatus status;
    private RFQBasicsDTO basics;
    private SpecificationDTO specification;
    private QcDTO qc;
    private CommercialTermsDTO commercialTerms;
    private SupplierCriteriaDTO supplierCriteria;
    private String selectedQuoteId;
}
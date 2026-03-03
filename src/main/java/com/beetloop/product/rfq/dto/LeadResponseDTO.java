package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class LeadResponseDTO {

    private String id;
    private String buyerId;
    private RFQBasicsDTO basics;
    private SpecificationDTO specification;
    private QcDTO qc;
    private CommercialTermsDTO commercialTerms;
    private SupplierCriteriaDTO supplierCriteria;
}
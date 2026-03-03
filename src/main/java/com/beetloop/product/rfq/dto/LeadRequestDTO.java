package com.beetloop.product.rfq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class LeadRequestDTO {
    private String id;
    private RFQBasicsDTO basics;
    private SpecificationDTO specification;
    private QcDTO qc;
    private CommercialTermsDTO commercialTerms;
    private SupplierCriteriaDTO supplierCriteria;
}
package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.dto.*;
import com.beetloop.product.rfq.enums.RfqStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rfqLeads")
@Data
public class LeadEntity {

    @Id
    private String id;

    private String buyerId;
    private RfqStatus status = RfqStatus.DRAFT;
    private RFQBasicsDTO basics;
    private SpecificationDTO specification;
    private QcDTO qc;
    private CommercialTermsDTO commercialTerms;
    private SupplierCriteriaDTO supplierCriteria;

    private Instant invitedAt;
    private Instant respondedAt;
    @LastModifiedDate
    private Instant updatedAt;
}
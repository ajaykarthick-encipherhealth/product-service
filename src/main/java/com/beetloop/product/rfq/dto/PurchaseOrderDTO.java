package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.POStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderDTO {
    private String id;
    private String poNumber;
    private String rfqId;
    private String quoteId;
    private String buyerId;
    private String vendorId;
    private POStatus status;

    private CompanyInfoDTO buyerEntity;
    private CompanyInfoDTO vendorEntity;
    private String billTo;
    private String shipTo;

    private String paymentTerms;
    private String taxStructure;

    private List<POLineItemDTO> lineItems;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String currency;

    private LocalDate expectedDeliveryDate;
    private String deliveryType;
    private String partialDeliveryAllowed;

    private List<PODocumentRequirementDTO> documentRequirements;

    private String authorizedBy;
    private String authorizedByRole;
    private String approvalRefId;
    private Instant authorizedAt;

    private LocalDate issueDate;
    private Integer validityDays;

    private Instant createdAt;
    private Instant updatedAt;
}

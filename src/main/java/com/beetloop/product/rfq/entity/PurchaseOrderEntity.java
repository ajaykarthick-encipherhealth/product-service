package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.enums.POStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "purchaseOrders")
@Data
public class PurchaseOrderEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String poNumber;

    @Indexed
    private String rfqId;
    @Indexed
    private String quoteId;

    private String buyerId;
    private String vendorId;

    private POStatus status = POStatus.DRAFT;

    private CompanyInfoEmbedded buyerEntity;
    private CompanyInfoEmbedded vendorEntity;
    private String billTo;
    private String shipTo;

    private String paymentTerms;
    private String taxStructure;

    private List<POLineItemEmbedded> lineItems = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private String currency;

    private LocalDate expectedDeliveryDate;
    private String deliveryType;
    private String partialDeliveryAllowed;

    private List<PODocumentRequirementEmbedded> documentRequirements = new ArrayList<>();

    private String authorizedBy;
    private String authorizedByRole;
    private String approvalRefId;
    private Instant authorizedAt;

    private LocalDate issueDate;
    private Integer validityDays;

    private Instant createdAt;
    private Instant updatedAt;
}

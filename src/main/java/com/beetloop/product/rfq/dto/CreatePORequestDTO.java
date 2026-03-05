package com.beetloop.product.rfq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Optional overrides when creating a PO from an approved quote.
 * If not provided, PO is built from RFQ + quote data.
 */
@Data
public class CreatePORequestDTO {
    private String buyerCompanyName;
    private String buyerAddress;
    private String buyerGstin;
    private String vendorCompanyName;
    private String vendorAddress;
    private String vendorGstin;
    private String billTo;
    private String shipTo;
    private String approvalRefId;
    private String authorizedBy;
    private String authorizedByRole;
}

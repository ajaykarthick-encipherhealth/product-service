package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.CreatePORequestDTO;
import com.beetloop.product.rfq.dto.PurchaseOrderDTO;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderDTO createFromApprovedRfq(String rfqId, String buyerId, CreatePORequestDTO overrides);

    PurchaseOrderDTO getById(String poId);

    PurchaseOrderDTO getByRfqId(String rfqId);

    /** Same as getById, for PDF preview / export. */
    PurchaseOrderDTO preview(String poId);

    PurchaseOrderDTO issuePo(String poId, String approvalRefId, String authorizedBy, String authorizedByRole);

    List<PurchaseOrderDTO> listByBuyerId(String buyerId);
}

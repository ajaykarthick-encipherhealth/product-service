package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.CreatePORequestDTO;
import com.beetloop.product.rfq.dto.PurchaseOrderDTO;
import com.beetloop.product.rfq.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /** Create a PO from an awarded RFQ (after finalize order). X-Buyer-Id required. */
    @PostMapping("/from-rfq/{rfqId}")
    public ResponseEntity<PurchaseOrderDTO> createFromRfq(
            @PathVariable String rfqId,
            @RequestHeader("X-Buyer-Id") String buyerId,
            @RequestBody(required = false) @Valid CreatePORequestDTO overrides) {
        PurchaseOrderDTO po = purchaseOrderService.createFromApprovedRfq(rfqId, buyerId, overrides);
        return ResponseEntity.status(HttpStatus.CREATED).body(po);
    }

    @GetMapping("/{poId}")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable String poId) {
        return ResponseEntity.ok(purchaseOrderService.getById(poId));
    }

    @GetMapping("/by-rfq/{rfqId}")
    public ResponseEntity<PurchaseOrderDTO> getByRfqId(@PathVariable String rfqId) {
        return ResponseEntity.ok(purchaseOrderService.getByRfqId(rfqId));
    }

    /** Get PO data for PDF preview / export. */
    @GetMapping("/{poId}/preview")
    public ResponseEntity<PurchaseOrderDTO> preview(@PathVariable String poId) {
        return ResponseEntity.ok(purchaseOrderService.preview(poId));
    }

    /** Issue the PO (DRAFT → ISSUED) after buyer confirmation. */
    @PostMapping("/{poId}/issue")
    public ResponseEntity<PurchaseOrderDTO> issuePo(
            @PathVariable String poId,
            @RequestParam String approvalRefId,
            @RequestParam String authorizedBy,
            @RequestParam String authorizedByRole) {
        return ResponseEntity.ok(purchaseOrderService.issuePo(poId, approvalRefId, authorizedBy, authorizedByRole));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<PurchaseOrderDTO>> listByBuyer(@PathVariable String buyerId) {
        return ResponseEntity.ok(purchaseOrderService.listByBuyerId(buyerId));
    }
}

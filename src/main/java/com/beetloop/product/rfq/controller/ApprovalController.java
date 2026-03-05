package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.ApprovalChainDTO;
import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.dto.SubmitApprovalRequestDTO;
import com.beetloop.product.rfq.service.ApprovalService;
import com.beetloop.product.rfq.service.QuoteActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final QuoteActionService quoteActionService;

    /** Get approval chain for an RFQ (e.g. Internal Approvals screen). */
    @GetMapping("/rfq/{rfqId}")
    public ResponseEntity<ApprovalChainDTO> getChainByRfqId(@PathVariable String rfqId) {
        return ResponseEntity.ok(approvalService.getChainByRfqId(rfqId));
    }

    /** Get approval chain by quote id. */
    @GetMapping("/quote/{quoteId}")
    public ResponseEntity<ApprovalChainDTO> getChainByQuoteId(@PathVariable String quoteId) {
        return ResponseEntity.ok(approvalService.getChainByQuoteId(quoteId));
    }

    /** Approver submits approval/rejection for a step (sequential). */
    @PostMapping("/chains/{chainId}/steps/{stepOrder}/submit")
    public ResponseEntity<ApprovalChainDTO> submitApproval(
            @PathVariable String chainId,
            @PathVariable int stepOrder,
            @RequestHeader("X-Approver-Id") String approverId,
            @RequestBody @Valid SubmitApprovalRequestDTO request) {
        return ResponseEntity.ok(approvalService.submitApproval(chainId, stepOrder, approverId, request));
    }

    /** After all steps approved, buyer finalizes order (quote → ACCEPTED, RFQ → AWARDED). */
    @PostMapping("/rfq/{rfqId}/finalize")
    public ResponseEntity<QuoteDTO> finalizeOrder(
            @PathVariable String rfqId,
            @RequestHeader("X-Buyer-Id") String buyerId) {
        return ResponseEntity.ok(quoteActionService.finalizeOrder(rfqId, buyerId));
    }
}

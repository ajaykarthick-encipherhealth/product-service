package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.dto.QuoteRequestDTO;
import com.beetloop.product.rfq.dto.RfqInviteDTO;
import com.beetloop.product.rfq.dto.RfqInviteRequestDTO;
import com.beetloop.product.rfq.service.QuoteActionService;
import com.beetloop.product.rfq.service.RfqInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RFQ invites: one document per vendor per RFQ in rfq_invites collection.
 * Only the RFQ buyer can create invites. Vendors submit initial quote via createQuote.
 */
@RestController
@RequestMapping("/api/v1/rfqs")
@RequiredArgsConstructor
public class RfqInviteController {

    private final RfqInviteService rfqInviteService;
    private final QuoteActionService quoteActionService;

    /**
     * Invite one or more vendors to an RFQ. Creates one rfq_invites document per vendor.
     * X-Buyer-Id header required (must be the RFQ owner).
     */
    @PostMapping("/{rfqId}/invites")
    public ResponseEntity<List<RfqInviteDTO>> inviteVendors(
            @PathVariable String rfqId,
            @RequestHeader("X-Buyer-Id") String buyerId,
            @RequestBody @Valid RfqInviteRequestDTO request) {
        List<RfqInviteDTO> invites = rfqInviteService.inviteVendors(rfqId, request.getVendorIds(), buyerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(invites);
    }

    /**
     * List all invites for an RFQ.
     */
    @GetMapping("/{rfqId}/invites")
    public ResponseEntity<List<RfqInviteDTO>> getInvitesByRfqId(@PathVariable String rfqId) {
        List<RfqInviteDTO> invites = rfqInviteService.getInvitesByRfqId(rfqId);
        return ResponseEntity.ok(invites);
    }

    /**
     * Get a single invite by id.
     */
    @GetMapping("/invites/{inviteId}")
    public ResponseEntity<RfqInviteDTO> getInviteById(@PathVariable String inviteId) {
        RfqInviteDTO invite = rfqInviteService.getInviteById(inviteId);
        return ResponseEntity.ok(invite);
    }

    /**
     * Vendor submits initial quote for this RFQ (vendor must be invited).
     * X-Vendor-Id header required. Body: quote pricing/line items (QuoteRequestDTO).
     */
    @PostMapping("/{rfqId}/quotes")
    public ResponseEntity<QuoteDTO> createQuote(
            @PathVariable String rfqId,
            @RequestHeader("X-Vendor-Id") String vendorId,
            @RequestBody(required = false) QuoteRequestDTO request) {
        QuoteDTO quote = quoteActionService.createQuote(vendorId, rfqId, request != null ? request : new QuoteRequestDTO());
        return ResponseEntity.status(HttpStatus.CREATED).body(quote);
    }
}

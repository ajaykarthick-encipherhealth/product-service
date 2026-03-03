package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.NegotiateQuoteRequestDTO;
import com.beetloop.product.rfq.dto.NegotiationMessageDTO;
import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.dto.QuoteRequestDTO;
import com.beetloop.product.rfq.dto.RejectQuoteRequestDTO;
import com.beetloop.product.rfq.enums.NegotiationRole;
import com.beetloop.product.rfq.service.QuoteActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteActionService quoteActionService;

    /** Finalize draft quote (DRAFT → SUBMITTED). */
    @PostMapping("/{quoteId}/submit")
    public ResponseEntity<QuoteDTO> submitQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-Vendor-Id") String vendorId) {
        QuoteDTO result = quoteActionService.submitQuote(quoteId, vendorId);
        return ResponseEntity.ok(result);
    }

    /** Get quote by id (e.g. to continue filling draft). */
    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteDTO> getQuote(@PathVariable String quoteId) {
        return ResponseEntity.ok(quoteActionService.getQuote(quoteId));
    }

    /** Patch draft quote with partial data. Only non-null fields in body are applied. X-Vendor-Id required. */
    @PatchMapping("/{quoteId}")
    public ResponseEntity<QuoteDTO> patchQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-Vendor-Id") String vendorId,
            @RequestBody(required = false) QuoteRequestDTO request) {
        QuoteDTO result = quoteActionService.patchQuote(quoteId, vendorId, request != null ? request : new QuoteRequestDTO());
        return ResponseEntity.ok(result);
    }

    /** Buyer accepts a quote. X-Buyer-Id header required. */
    @PostMapping("/{quoteId}/accept")
    public ResponseEntity<QuoteDTO> acceptQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-Buyer-Id") String buyerId) {
        QuoteDTO result = quoteActionService.acceptQuote(quoteId, buyerId);
        return ResponseEntity.ok(result);
    }

    /** Buyer rejects a quote. X-Buyer-Id header required. */
    @PostMapping("/{quoteId}/reject")
    public ResponseEntity<QuoteDTO> rejectQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-Buyer-Id") String buyerId,
            @RequestBody(required = false) RejectQuoteRequestDTO body) {
        String reason = body != null ? body.getReason() : null;
        QuoteDTO result = quoteActionService.rejectQuote(quoteId, reason != null ? reason : "", buyerId);
        return ResponseEntity.ok(result);
    }

    /** Buyer or vendor adds a negotiation message. X-Author-Id and X-Role (BUYER|VENDOR) required. */
    @PostMapping("/{quoteId}/negotiate")
    public ResponseEntity<NegotiationMessageDTO> negotiateQuote(
            @PathVariable String quoteId,
            @RequestHeader("X-Author-Id") String authorId,
            @RequestHeader("X-Role") NegotiationRole role,
            @RequestBody @Valid NegotiateQuoteRequestDTO request) {
        NegotiationMessageDTO result = quoteActionService.negotiateQuote(
                quoteId, request.getMessage(), authorId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /** Vendor submits a revised quote (new quote document). X-Vendor-Id header required. */
    @PostMapping("/{previousQuoteId}/revised")
    public ResponseEntity<QuoteDTO> submitRevisedQuote(
            @PathVariable String previousQuoteId,
            @RequestHeader("X-Vendor-Id") String vendorId,
            @RequestBody @Valid QuoteRequestDTO request) {
        QuoteDTO result = quoteActionService.submitRevisedQuote(previousQuoteId, vendorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /** Get negotiation thread for a quote (buyer or vendor). */
    @GetMapping("/{quoteId}/negotiations")
    public ResponseEntity<List<NegotiationMessageDTO>> getNegotiationMessages(@PathVariable String quoteId) {
        List<NegotiationMessageDTO> messages = quoteActionService.getNegotiationMessages(quoteId);
        return ResponseEntity.ok(messages);
    }
}

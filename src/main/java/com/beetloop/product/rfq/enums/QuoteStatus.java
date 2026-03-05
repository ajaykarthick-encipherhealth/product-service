package com.beetloop.product.rfq.enums;

/**
 * Quote status. Revised quotes are new documents linked via previousQuoteId.
 * Transitions: draft → submitted (vendor finalizes); submitted → selected | rejected | under_negotiation;
 * selected → accepted (after all internal approvals) | (buyer can change selection);
 * under_negotiation → accepted | rejected | (vendor submits revised quote)
 */
public enum QuoteStatus {
    DRAFT,
    SUBMITTED,
    SELECTED,           /** Buyer selected this quote for internal approval; not yet accepted */
    UNDER_NEGOTIATION,
    ACCEPTED,
    REJECTED
}

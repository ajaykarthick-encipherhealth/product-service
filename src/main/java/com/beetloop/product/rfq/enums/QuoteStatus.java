package com.beetloop.product.rfq.enums;

/**
 * Quote status. Revised quotes are new documents linked via previousQuoteId.
 * Transitions: draft → submitted (vendor finalizes); submitted → accepted | rejected | under_negotiation;
 * under_negotiation → accepted | rejected | (vendor submits revised quote)
 */
public enum QuoteStatus {
    DRAFT,
    SUBMITTED,
    UNDER_NEGOTIATION,
    ACCEPTED,
    REJECTED
}

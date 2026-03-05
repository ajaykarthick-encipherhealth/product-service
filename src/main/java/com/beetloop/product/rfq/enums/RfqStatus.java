package com.beetloop.product.rfq.enums;

/**
 * RFQ lifecycle status.
 * Transitions: draft → open → pending_approval (quote selected) → awarded | closed
 */
public enum RfqStatus {
    DRAFT,
    OPEN,
    PENDING_APPROVAL,    /** Buyer selected a quote; awaiting internal approval chain */
    AWARDED,
    CLOSED
}

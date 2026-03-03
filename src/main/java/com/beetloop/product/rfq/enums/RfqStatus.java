package com.beetloop.product.rfq.enums;

/**
 * RFQ lifecycle status.
 * Transitions: draft → open → awarded | closed
 */
public enum RfqStatus {
    DRAFT,
    OPEN,
    AWARDED,
    CLOSED
}

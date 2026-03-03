package com.beetloop.product.rfq.enums;

/**
 * RFQ invite status per vendor.
 * Transitions: invited → responded | rejected | awarded
 */
public enum RfqInviteStatus {
    INVITED,
    RESPONDED,
    REJECTED,
    AWARDED
}

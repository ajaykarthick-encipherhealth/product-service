package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.RfqInviteStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class RfqInviteDTO {

    private String id;
    private String rfqId;
    private String vendorId;
    private RfqInviteStatus status;
    private String latestQuoteId;
    private Instant invitedAt;
    private Instant respondedAt;
    private Instant updatedAt;
}

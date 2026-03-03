package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.enums.RfqInviteStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rfq_invites")
@CompoundIndex(name = "rfq_vendor_unique", def = "{'rfqId': 1, 'vendorId': 1}", unique = true)
@Data
public class RfqInviteEntity {

    @Id
    private String id;

    private String rfqId;
    private String vendorId;

    private RfqInviteStatus status = RfqInviteStatus.INVITED;

    /** Latest quote id for this invite (vendor may have multiple quotes; this points to current/relevant one). */
    private String latestQuoteId;

    private Instant invitedAt;
    private Instant respondedAt;
    @LastModifiedDate
    private Instant updatedAt;
}

package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.RfqInviteDTO;

import java.util.List;

/**
 * Manages RFQ invites: one document per vendor per RFQ in rfq_invites collection.
 */
public interface RfqInviteService {

    /**
     * Invite one or more vendors to an RFQ. Creates one rfq_invites document per vendor.
     * Only the RFQ buyer may invite. RFQ must exist and not be awarded.
     */
    List<RfqInviteDTO> inviteVendors(String rfqId, List<String> vendorIds, String buyerId);

    /**
     * List all invites for an RFQ (buyer or invited vendor).
     */
    List<RfqInviteDTO> getInvitesByRfqId(String rfqId);

    /**
     * Get a single invite by id.
     */
    RfqInviteDTO getInviteById(String inviteId);
}

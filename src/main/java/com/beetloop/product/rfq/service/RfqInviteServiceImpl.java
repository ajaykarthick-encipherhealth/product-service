package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.RfqInviteDTO;
import com.beetloop.product.rfq.entity.LeadEntity;
import com.beetloop.product.rfq.entity.RfqEntity;
import com.beetloop.product.rfq.entity.RfqInviteEntity;
import com.beetloop.product.rfq.enums.RfqInviteStatus;
import com.beetloop.product.rfq.enums.RfqStatus;
import com.beetloop.product.rfq.exception.ForbiddenException;
import com.beetloop.product.rfq.exception.QuoteActionException;
import com.beetloop.product.rfq.mapper.RfqInviteMapper;
import com.beetloop.product.rfq.repository.LeadRepository;
import com.beetloop.product.rfq.repository.RfqInviteRepository;
import com.beetloop.product.rfq.repository.RfqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfqInviteServiceImpl implements RfqInviteService {

    private final LeadRepository rfqRepository;
    private final RfqInviteRepository rfqInviteRepository;
    private final RfqInviteMapper rfqInviteMapper;

    @Override
    @Transactional
    public List<RfqInviteDTO> inviteVendors(String rfqId, List<String> vendorIds, String buyerId) {
        LeadEntity rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + rfqId));
        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new ForbiddenException("Only the RFQ buyer can invite vendors");
        }
        if (rfq.getStatus() == RfqStatus.AWARDED) {
            throw new QuoteActionException("Cannot invite vendors to an awarded RFQ");
        }

        Instant now = Instant.now();
        List<RfqInviteDTO> result = new ArrayList<>();
        for (String vendorId : vendorIds) {
            if (vendorId == null || vendorId.isBlank()) continue;
            RfqInviteEntity invite = rfqInviteRepository.findByRfqIdAndVendorId(rfqId, vendorId).orElse(null);
            if (invite != null) {
                result.add(rfqInviteMapper.toDto(invite));
                continue;
            }
            invite = new RfqInviteEntity();
            invite.setRfqId(rfqId);
            invite.setVendorId(vendorId);
            invite.setStatus(RfqInviteStatus.INVITED);
            invite.setInvitedAt(now);
            invite.setUpdatedAt(now);
            invite = rfqInviteRepository.save(invite);
            result.add(rfqInviteMapper.toDto(invite));
        }

        if (rfq.getStatus() == RfqStatus.DRAFT) {
            rfq.setStatus(RfqStatus.OPEN);
            rfq.setUpdatedAt(now);
            rfqRepository.save(rfq);
        }
        return result;
    }

    @Override
    public List<RfqInviteDTO> getInvitesByRfqId(String rfqId) {
        return rfqInviteRepository.findByRfqId(rfqId).stream()
                .map(rfqInviteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RfqInviteDTO getInviteById(String inviteId) {
        RfqInviteEntity invite = rfqInviteRepository.findById(inviteId)
                .orElseThrow(() -> new QuoteActionException("Invite not found: " + inviteId));
        return rfqInviteMapper.toDto(invite);
    }
}

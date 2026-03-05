package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.ApprovalChainRequestDTO;
import com.beetloop.product.rfq.dto.NegotiationMessageDTO;
import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.dto.QuoteRequestDTO;
import com.beetloop.product.rfq.entity.*;
import com.beetloop.product.rfq.enums.NegotiationRole;
import com.beetloop.product.rfq.enums.QuoteStatus;
import com.beetloop.product.rfq.enums.RfqInviteStatus;
import com.beetloop.product.rfq.enums.RfqStatus;
import com.beetloop.product.rfq.service.ApprovalService;
import com.beetloop.product.rfq.exception.ForbiddenException;
import com.beetloop.product.rfq.exception.QuoteActionException;
import com.beetloop.product.rfq.mapper.NegotiationMessageMapper;
import com.beetloop.product.rfq.mapper.QuoteMapper;
import com.beetloop.product.rfq.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteActionServiceImpl implements QuoteActionService {

    private final QuoteRepository quoteRepository;
    private final LeadRepository rfqRepository;
    private final RfqInviteRepository rfqInviteRepository;
    private final NegotiationMessageRepository negotiationMessageRepository;
    private final QuoteMapper quoteMapper;
    private final NegotiationMessageMapper negotiationMessageMapper;
    private final ApprovalService approvalService;

    @Override
    @Transactional
    public QuoteDTO createQuote(String vendorId, String rfqId, QuoteRequestDTO request) {
        RfqInviteEntity invite = rfqInviteRepository.findByRfqIdAndVendorId(rfqId, vendorId)
                .orElseThrow(() -> new QuoteActionException("Vendor is not invited to this RFQ"));
        if (invite.getStatus() != RfqInviteStatus.INVITED) {
            throw new QuoteActionException("Quote already submitted for this invite; use revised quote for updates");
        }

        LeadEntity rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + rfqId));
        if (rfq.getStatus() == RfqStatus.AWARDED) {
            throw new QuoteActionException("Cannot submit quote to an awarded RFQ");
        }

        Instant now = Instant.now();
        QuoteEntity quote = quoteRepository.findFirstByRfqIdAndVendorIdAndStatus(rfqId, vendorId, QuoteStatus.DRAFT)
                .orElse(null);

        if (quote != null) {
            mergeRequestIntoQuote(quote, request);
            quote.setUpdatedAt(now);
            quote = quoteRepository.save(quote);
            return quoteMapper.toDto(quote);
        }

        quote = new QuoteEntity();
        quote.setRfqId(rfqId);
        quote.setVendorId(vendorId);
        quote.setRfqInviteId(invite.getId());
        quote.setStatus(QuoteStatus.DRAFT);
        quote.setVersion(1);
        quote.setUpdatedAt(now);
        mergeRequestIntoQuote(quote, request);
        quote = quoteRepository.save(quote);

        return quoteMapper.toDto(quote);
    }

    private void mergeRequestIntoQuote(QuoteEntity quote, QuoteRequestDTO request) {
        if (request == null) return;
        if (request.getQuoteCommercialTermsDTO() != null) {
            quote.setQuoteCommercialTerms(request.getQuoteCommercialTermsDTO());
        }
        if (request.getQuoteInfo() != null) {
            quote.setQuoteInfo(request.getQuoteInfo());
        }
        if (request.getNotes() != null) {
            quote.setNotes(request.getNotes());
        }
    }

    @Override
    @Transactional
    public QuoteDTO submitQuote(String quoteId, String vendorId) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));
        if (!quote.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Only the quote vendor can submit this quote");
        }
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new QuoteActionException("Only a draft quote can be submitted; current status: " + quote.getStatus());
        }

        Instant now = Instant.now();
        quote.setStatus(QuoteStatus.SUBMITTED);
        quote.setSubmittedAt(now);
        quote.setUpdatedAt(now);
        quote = quoteRepository.save(quote);

        RfqInviteEntity invite = rfqInviteRepository.findById(quote.getRfqInviteId())
                .orElseThrow(() -> new QuoteActionException("RFQ invite not found"));
        invite.setStatus(RfqInviteStatus.RESPONDED);
        invite.setRespondedAt(now);
        invite.setLatestQuoteId(quoteId);
        invite.setUpdatedAt(now);
        rfqInviteRepository.save(invite);

        return quoteMapper.toDto(quote);
    }

    @Override
    @Transactional
    public QuoteDTO patchQuote(String quoteId, String vendorId, QuoteRequestDTO request) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));
        if (!quote.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Only the quote vendor can patch this quote");
        }
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new QuoteActionException("Only a draft quote can be patched; current status: " + quote.getStatus());
        }
        mergeRequestIntoQuote(quote, request != null ? request : new QuoteRequestDTO());
        quote.setUpdatedAt(Instant.now());
        quote = quoteRepository.save(quote);
        return quoteMapper.toDto(quote);
    }

    @Override
    public QuoteDTO getQuote(String quoteId) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));
        return quoteMapper.toDto(quote);
    }

    @Override
    public List<QuoteDTO> listQuotesByRfqId(String rfqId) {
        return quoteRepository.findByRfqId(rfqId).stream()
                .map(quoteMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuoteDTO selectQuoteForApproval(String quoteId, String buyerId, ApprovalChainRequestDTO approvalChainRequest) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));
        LeadEntity rfq = rfqRepository.findById(quote.getRfqId())
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + quote.getRfqId()));
        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new ForbiddenException("Only the RFQ buyer can select a quote for approval");
        }
        if (rfq.getStatus() == RfqStatus.AWARDED) {
            throw new QuoteActionException("RFQ is already awarded");
        }
        if (quote.getStatus() != QuoteStatus.SUBMITTED && quote.getStatus() != QuoteStatus.UNDER_NEGOTIATION) {
            throw new QuoteActionException("Quote must be submitted or under negotiation to be selected for approval");
        }
        if (approvalChainRequest == null || approvalChainRequest.getSteps() == null || approvalChainRequest.getSteps().isEmpty()) {
            throw new QuoteActionException("Approval chain steps are required when selecting a quote for approval");
        }
        Instant now = Instant.now();
        quote.setStatus(QuoteStatus.SELECTED);
        quote.setUpdatedAt(now);
        quoteRepository.save(quote);
        rfq.setSelectedQuoteId(quoteId);
        rfq.setStatus(RfqStatus.PENDING_APPROVAL);
        rfq.setUpdatedAt(now);
        rfqRepository.save(rfq);
        approvalService.createChain(rfq.getId(), quoteId, approvalChainRequest);
        return quoteMapper.toDto(quote);
    }

    @Override
    @Transactional
    public QuoteDTO finalizeOrder(String rfqId, String buyerId) {
        LeadEntity rfq = rfqRepository.findById(rfqId)
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + rfqId));
        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new ForbiddenException("Only the RFQ buyer can finalize the order");
        }
        if (rfq.getStatus() != RfqStatus.PENDING_APPROVAL) {
            throw new QuoteActionException("RFQ must be in PENDING_APPROVAL to finalize; current status: " + rfq.getStatus());
        }
        String selectedQuoteId = rfq.getSelectedQuoteId();
        if (selectedQuoteId == null || selectedQuoteId.isBlank()) {
            throw new QuoteActionException("No quote selected for this RFQ");
        }
        if (!approvalService.isFullyApproved(rfqId)) {
            throw new QuoteActionException("All approval steps must be completed before finalizing the order");
        }
        return acceptQuote(selectedQuoteId, buyerId);
    }

    @Override
    @Transactional
    public QuoteDTO acceptQuote(String quoteId, String buyerId) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));

        LeadEntity rfq = rfqRepository.findById(quote.getRfqId())
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + quote.getRfqId()));

        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new ForbiddenException("Only the RFQ buyer can accept a quote");
        }
        if (rfq.getStatus() == RfqStatus.AWARDED) {
            throw new QuoteActionException("RFQ is already awarded");
        }
        if (quote.getStatus() != QuoteStatus.SUBMITTED && quote.getStatus() != QuoteStatus.UNDER_NEGOTIATION && quote.getStatus() != QuoteStatus.SELECTED) {
            throw new QuoteActionException("Quote must be submitted, under negotiation, or selected (post-approval) to be accepted");
        }

        quote.setStatus(QuoteStatus.ACCEPTED);
        quote.setUpdatedAt(Instant.now());
        quoteRepository.save(quote);

        List<QuoteEntity> otherQuotes = quoteRepository.findByRfqId(quote.getRfqId()).stream()
                .filter(q -> !q.getId().equals(quoteId))
                .collect(Collectors.toList());
        for (QuoteEntity other : otherQuotes) {
            if (other.getStatus() != QuoteStatus.ACCEPTED) {
                other.setStatus(QuoteStatus.REJECTED);
                other.setUpdatedAt(Instant.now());
                quoteRepository.save(other);
            }
        }

        rfq.setStatus(RfqStatus.AWARDED);
        rfq.setUpdatedAt(Instant.now());
        rfqRepository.save(rfq);

        RfqInviteEntity invite = rfqInviteRepository.findByRfqIdAndVendorId(quote.getRfqId(), quote.getVendorId())
                .orElseThrow(() -> new QuoteActionException("RFQ invite not found"));
        invite.setStatus(RfqInviteStatus.AWARDED);
        invite.setLatestQuoteId(quoteId);
        invite.setUpdatedAt(Instant.now());
        rfqInviteRepository.save(invite);

        List<RfqInviteEntity> otherInvites = rfqInviteRepository.findByRfqId(quote.getRfqId()).stream()
                .filter(i -> !i.getVendorId().equals(quote.getVendorId()))
                .collect(Collectors.toList());
        for (RfqInviteEntity otherInv : otherInvites) {
            if (otherInv.getStatus() != RfqInviteStatus.AWARDED) {
                otherInv.setStatus(RfqInviteStatus.REJECTED);
                otherInv.setUpdatedAt(Instant.now());
                rfqInviteRepository.save(otherInv);
            }
        }

        return quoteMapper.toDto(quote);
    }

    @Override
    @Transactional
    public QuoteDTO rejectQuote(String quoteId, String reason, String buyerId) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));

        LeadEntity rfq = rfqRepository.findById(quote.getRfqId())
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + quote.getRfqId()));

        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new ForbiddenException("Only the RFQ buyer can reject a quote");
        }
        if (quote.getStatus() == QuoteStatus.ACCEPTED) {
            throw new QuoteActionException("Cannot reject an accepted quote");
        }

        quote.setStatus(QuoteStatus.REJECTED);
        quote.setNotes(quote.getNotes() != null ? quote.getNotes() + "\nRejection reason: " + reason : "Rejection reason: " + reason);
        quote.setUpdatedAt(Instant.now());
        quoteRepository.save(quote);

        rfqInviteRepository.findByRfqIdAndVendorId(quote.getRfqId(), quote.getVendorId()).ifPresent(invite -> {
            invite.setStatus(RfqInviteStatus.REJECTED);
            invite.setUpdatedAt(Instant.now());
            rfqInviteRepository.save(invite);
        });

        return quoteMapper.toDto(quote);
    }

    @Override
    @Transactional
    public NegotiationMessageDTO negotiateQuote(String quoteId, String message, String authorId, NegotiationRole role) {
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));

        LeadEntity rfq = rfqRepository.findById(quote.getRfqId())
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + quote.getRfqId()));

        if (role == NegotiationRole.BUYER && !rfq.getBuyerId().equals(authorId)) {
            throw new ForbiddenException("Only the RFQ buyer can send negotiation messages as buyer");
        }
        if (role == NegotiationRole.VENDOR && !quote.getVendorId().equals(authorId)) {
            throw new ForbiddenException("Only the quote vendor can send negotiation messages as vendor");
        }
        if (quote.getStatus() != QuoteStatus.SUBMITTED && quote.getStatus() != QuoteStatus.UNDER_NEGOTIATION) {
            throw new QuoteActionException("Quote must be submitted or under negotiation to add messages");
        }

        quote.setStatus(QuoteStatus.UNDER_NEGOTIATION);
        quote.setUpdatedAt(Instant.now());
        quoteRepository.save(quote);

        NegotiationMessageEntity msg = new NegotiationMessageEntity();
        msg.setQuoteId(quoteId);
        msg.setRfqId(quote.getRfqId());
        msg.setRole(role);
        msg.setAuthorId(authorId);
        msg.setMessage(message);
        msg.setCreatedAt(Instant.now());
        msg = negotiationMessageRepository.save(msg);

        return negotiationMessageMapper.toDto(msg);
    }

    @Override
    @Transactional
    public QuoteDTO submitRevisedQuote(String previousQuoteId, String vendorId, QuoteRequestDTO request) {
        if (request == null) {
            throw new QuoteActionException("Quote request body is required for revised quote");
        }
        QuoteEntity previousQuote = quoteRepository.findById(previousQuoteId)
                .orElseThrow(() -> new QuoteActionException("Previous quote not found: " + previousQuoteId));

        if (!previousQuote.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("Only the vendor of the original quote can submit a revision");
        }
        if (previousQuote.getStatus() != QuoteStatus.UNDER_NEGOTIATION) {
            throw new QuoteActionException("Revised quote can only be submitted when the quote is under negotiation");
        }

        QuoteEntity revised = new QuoteEntity();
        revised.setRfqId(previousQuote.getRfqId());
        revised.setVendorId(previousQuote.getVendorId());
        revised.setRfqInviteId(previousQuote.getRfqInviteId());
        revised.setStatus(QuoteStatus.SUBMITTED);
        revised.setPreviousQuoteId(previousQuoteId);
        revised.setVersion(previousQuote.getVersion() + 1);
        revised.setNotes(request.getNotes());
        revised.setQuoteCommercialTerms(request.getQuoteCommercialTermsDTO());
        revised.setQuoteInfo(request.getQuoteInfo());
        revised.setSubmittedAt(Instant.now());
        revised.setUpdatedAt(Instant.now());
        revised = quoteRepository.save(revised);

        RfqInviteEntity invite = rfqInviteRepository.findById(previousQuote.getRfqInviteId()).orElse(null);
        if (invite != null) {
            invite.setLatestQuoteId(revised.getId());
            invite.setUpdatedAt(Instant.now());
            rfqInviteRepository.save(invite);
        }

        return quoteMapper.toDto(revised);
    }

    @Override
    public List<NegotiationMessageDTO> getNegotiationMessages(String quoteId) {
        return negotiationMessageRepository.findByQuoteIdOrderByCreatedAtAsc(quoteId).stream()
                .map(negotiationMessageMapper::toDto)
                .collect(Collectors.toList());
    }
}

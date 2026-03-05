package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.ApprovalChainDTO;
import com.beetloop.product.rfq.dto.ApprovalChainRequestDTO;
import com.beetloop.product.rfq.dto.ApprovalStepDTO;
import com.beetloop.product.rfq.dto.SubmitApprovalRequestDTO;
import com.beetloop.product.rfq.entity.ApprovalChainEntity;
import com.beetloop.product.rfq.entity.ApprovalStepEmbedded;
import com.beetloop.product.rfq.enums.ApprovalChainStatus;
import com.beetloop.product.rfq.enums.ApprovalStepStatus;
import com.beetloop.product.rfq.exception.QuoteActionException;
import com.beetloop.product.rfq.repository.ApprovalChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalChainRepository approvalChainRepository;

    @Override
    @Transactional
    public ApprovalChainDTO createChain(String rfqId, String quoteId, ApprovalChainRequestDTO request) {
        if (approvalChainRepository.existsByRfqId(rfqId)) {
            throw new QuoteActionException("Approval chain already exists for this RFQ");
        }
        Instant now = Instant.now();
        ApprovalChainEntity entity = new ApprovalChainEntity();
        entity.setRfqId(rfqId);
        entity.setQuoteId(quoteId);
        entity.setStatus(ApprovalChainStatus.PENDING);
        entity.setInitiatedAt(now);
        entity.setLastUpdatedAt(now);
        List<ApprovalStepEmbedded> steps = request.getSteps().stream()
                .map(s -> {
                    ApprovalStepEmbedded step = new ApprovalStepEmbedded();
                    step.setApproverId(s.getApproverId());
                    step.setApproverName(s.getApproverName());
                    step.setRole(s.getRole());
                    step.setOrder(s.getOrder());
                    step.setStatus(ApprovalStepStatus.PENDING);
                    return step;
                })
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .collect(Collectors.toList());
        entity.setSteps(steps);
        entity = approvalChainRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public ApprovalChainDTO getChainByRfqId(String rfqId) {
        ApprovalChainEntity entity = approvalChainRepository.findByRfqId(rfqId)
                .orElseThrow(() -> new QuoteActionException("Approval chain not found for RFQ: " + rfqId));
        return toDto(entity);
    }

    @Override
    public ApprovalChainDTO getChainByQuoteId(String quoteId) {
        ApprovalChainEntity entity = approvalChainRepository.findByQuoteId(quoteId)
                .orElseThrow(() -> new QuoteActionException("Approval chain not found for quote: " + quoteId));
        return toDto(entity);
    }

    @Override
    @Transactional
    public ApprovalChainDTO submitApproval(String chainId, int stepOrder, String approverId, SubmitApprovalRequestDTO request) {
        ApprovalChainEntity chain = approvalChainRepository.findById(chainId)
                .orElseThrow(() -> new QuoteActionException("Approval chain not found: " + chainId));
        if (chain.getStatus() != ApprovalChainStatus.PENDING) {
            throw new QuoteActionException("Approval chain is no longer pending");
        }
        List<ApprovalStepEmbedded> steps = chain.getSteps();
        ApprovalStepEmbedded step = steps.stream()
                .filter(s -> s.getOrder() == stepOrder)
                .findFirst()
                .orElseThrow(() -> new QuoteActionException("Step order not found: " + stepOrder));
        if (!step.getApproverId().equals(approverId)) {
            throw new QuoteActionException("Only the designated approver can approve this step");
        }
        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new QuoteActionException("This step is already " + step.getStatus());
        }
        // Sequential: all previous steps must be approved
        boolean previousApproved = steps.stream()
                .filter(s -> s.getOrder() < stepOrder)
                .allMatch(s -> s.getStatus() == ApprovalStepStatus.APPROVED);
        if (!previousApproved) {
            throw new QuoteActionException("Previous approval steps must be completed first");
        }
        Instant now = Instant.now();
        step.setStatus(request.isApproved() ? ApprovalStepStatus.APPROVED : ApprovalStepStatus.REJECTED);
        step.setApprovedAt(now);
        if (request.getNotes() != null) {
            step.setNotes(request.getNotes());
        }
        chain.setLastUpdatedAt(now);
        if (!request.isApproved()) {
            chain.setStatus(ApprovalChainStatus.REJECTED);
        } else if (steps.stream().allMatch(s -> s.getStatus() == ApprovalStepStatus.APPROVED)) {
            chain.setStatus(ApprovalChainStatus.APPROVED);
        }
        chain = approvalChainRepository.save(chain);
        return toDto(chain);
    }

    @Override
    public boolean isFullyApproved(String rfqId) {
        return approvalChainRepository.findByRfqId(rfqId)
                .map(c -> c.getStatus() == ApprovalChainStatus.APPROVED)
                .orElse(false);
    }

    private static ApprovalChainDTO toDto(ApprovalChainEntity entity) {
        ApprovalChainDTO dto = new ApprovalChainDTO();
        dto.setId(entity.getId());
        dto.setRfqId(entity.getRfqId());
        dto.setQuoteId(entity.getQuoteId());
        dto.setStatus(entity.getStatus());
        dto.setInitiatedAt(entity.getInitiatedAt());
        dto.setLastUpdatedAt(entity.getLastUpdatedAt());
        if (entity.getSteps() != null) {
            dto.setSteps(entity.getSteps().stream().map(ApprovalServiceImpl::stepToDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private static ApprovalStepDTO stepToDto(ApprovalStepEmbedded s) {
        ApprovalStepDTO dto = new ApprovalStepDTO();
        dto.setApproverId(s.getApproverId());
        dto.setApproverName(s.getApproverName());
        dto.setRole(s.getRole());
        dto.setOrder(s.getOrder());
        dto.setStatus(s.getStatus());
        dto.setApprovedAt(s.getApprovedAt());
        dto.setNotes(s.getNotes());
        return dto;
    }
}

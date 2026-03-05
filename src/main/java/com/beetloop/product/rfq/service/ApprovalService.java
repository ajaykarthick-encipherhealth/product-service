package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.ApprovalChainDTO;
import com.beetloop.product.rfq.dto.ApprovalChainRequestDTO;
import com.beetloop.product.rfq.dto.SubmitApprovalRequestDTO;

import java.util.List;

public interface ApprovalService {

    ApprovalChainDTO createChain(String rfqId, String quoteId, ApprovalChainRequestDTO request);

    ApprovalChainDTO getChainByRfqId(String rfqId);

    ApprovalChainDTO getChainByQuoteId(String quoteId);

    ApprovalChainDTO submitApproval(String chainId, int stepOrder, String approverId, SubmitApprovalRequestDTO request);

    boolean isFullyApproved(String rfqId);
}

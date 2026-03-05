package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.ApprovalChainStatus;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ApprovalChainDTO {
    private String id;
    private String rfqId;
    private String quoteId;
    private ApprovalChainStatus status;
    private List<ApprovalStepDTO> steps;
    private Instant initiatedAt;
    private Instant lastUpdatedAt;
}

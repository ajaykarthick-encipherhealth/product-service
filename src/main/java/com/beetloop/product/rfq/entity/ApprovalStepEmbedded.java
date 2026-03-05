package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.enums.ApprovalStepStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class ApprovalStepEmbedded {
    private String approverId;
    private String approverName;
    private String role;
    private int order;
    private ApprovalStepStatus status = ApprovalStepStatus.PENDING;
    private Instant approvedAt;
    private String notes;
}

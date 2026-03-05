package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.ApprovalStepStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class ApprovalStepDTO {
    private String approverId;
    private String approverName;
    private String role;
    private int order;
    private ApprovalStepStatus status;
    private Instant approvedAt;
    private String notes;
}

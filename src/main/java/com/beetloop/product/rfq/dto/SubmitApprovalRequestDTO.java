package com.beetloop.product.rfq.dto;

import lombok.Data;

@Data
public class SubmitApprovalRequestDTO {
    private boolean approved;  // true = approve, false = reject
    private String notes;
}

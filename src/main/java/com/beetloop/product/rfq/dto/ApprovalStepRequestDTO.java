package com.beetloop.product.rfq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovalStepRequestDTO {
    @NotBlank
    private String approverId;
    @NotBlank
    private String approverName;
    @NotBlank
    private String role;
    private int order;
}

package com.beetloop.product.rfq.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ApprovalChainRequestDTO {
    @NotEmpty
    @Valid
    private List<ApprovalStepRequestDTO> steps;
}

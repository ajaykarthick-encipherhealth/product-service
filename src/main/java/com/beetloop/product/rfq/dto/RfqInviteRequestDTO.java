package com.beetloop.product.rfq.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RfqInviteRequestDTO {

    @NotEmpty(message = "At least one vendor must be invited")
    private List<String> vendorIds;
}

package com.beetloop.product.rfq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NegotiateQuoteRequestDTO {

    @NotBlank(message = "Message is required")
    private String message;
}

package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.NegotiationRole;
import lombok.Data;

import java.time.Instant;

@Data
public class NegotiationMessageDTO {

    private String id;
    private String quoteId;
    private String rfqId;
    private NegotiationRole role;
    private String authorId;
    private String message;
    private Instant createdAt;
}

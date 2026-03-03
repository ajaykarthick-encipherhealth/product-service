package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.enums.QuoteStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
public class QuoteDTO {

    private String id;
    private String rfqId;
    private String vendorId;
    private String rfqInviteId;
    private QuoteStatus status;
    private String previousQuoteId;
    private Integer version;

    private BigDecimal totalPrice;
    private String currency;
    private Map<String, Object> lineItems;
    private String notes;

    private Instant submittedAt;
    private Instant updatedAt;

    private QuoteCommercialTermsDTO quoteCommercialTermsDTO;
    private QuoteDetails quoteInfo;
}

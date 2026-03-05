package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CommercialTermsPricing {
    private String paymentTerms;
    private String quoteValidityPeriod;
    private String deliveryTerms;
    private String currency;
    private List<VolumeBasedPricing> volumeBasedPricings;
    private String additionalTerms;
    /** Unit price per quantity (e.g. per kg). Used for PO line items. */
    private BigDecimal unitPrice;
    /** Total quote value. Used for PO total. */
    private BigDecimal totalQuote;
    /** Quantity quoted (e.g. 500). */
    private String quantity;
    /** Unit (e.g. kg). */
    private String unit;
}

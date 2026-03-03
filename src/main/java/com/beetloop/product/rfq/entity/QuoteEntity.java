package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.dto.QuoteCommercialTermsDTO;
import com.beetloop.product.rfq.dto.QuoteDetails;
import com.beetloop.product.rfq.enums.QuoteStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Document(collection = "quotes")
@Data
public class QuoteEntity {

    @Id
    private String id;

    @Indexed
    private String rfqId;
    @Indexed
    private String vendorId;
    @Indexed
    private String rfqInviteId;

    private QuoteStatus status = QuoteStatus.DRAFT;

    /** When this quote is a revision, link to the previous quote (preserves history). */
    private String previousQuoteId;

    /** Quote version number: 1 for initial, 2+ for revisions. */
    private Integer version = 1;

    /** Commercial terms / pricing offered by vendor (extensible). */
    private String notes;

    /** Commercial terms (pricing, SLA) – submitted step by step. */
    private QuoteCommercialTermsDTO quoteCommercialTerms;
    /** Quote sections / details – submitted step by step. */
    private QuoteDetails quoteInfo;

    private Instant submittedAt;
    @LastModifiedDate
    private Instant updatedAt;
    @Version
    private Long documentVersion;
}

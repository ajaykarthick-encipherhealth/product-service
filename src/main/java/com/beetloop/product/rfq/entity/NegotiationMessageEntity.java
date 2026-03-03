package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.enums.NegotiationRole;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "negotiations")
@Data
public class NegotiationMessageEntity {

    @Id
    private String id;

    @Indexed
    private String quoteId;
    @Indexed
    private String rfqId;

    private NegotiationRole role;
    private String authorId;
    private String message;

    private Instant createdAt;
}

package com.beetloop.product.rfq.entity;

import com.beetloop.product.rfq.enums.ApprovalChainStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "approvalChains")
@Data
public class ApprovalChainEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String rfqId;
    @Indexed
    private String quoteId;

    private ApprovalChainStatus status = ApprovalChainStatus.PENDING;
    private List<ApprovalStepEmbedded> steps = new ArrayList<>();

    private Instant initiatedAt;
    private Instant lastUpdatedAt;
}

package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.ApprovalChainEntity;
import com.beetloop.product.rfq.enums.ApprovalChainStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalChainRepository extends MongoRepository<ApprovalChainEntity, String> {

    Optional<ApprovalChainEntity> findByRfqId(String rfqId);
    Optional<ApprovalChainEntity> findByQuoteId(String quoteId);
    boolean existsByRfqId(String rfqId);
}

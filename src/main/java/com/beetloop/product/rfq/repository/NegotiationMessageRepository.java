package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.NegotiationMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationMessageRepository extends MongoRepository<NegotiationMessageEntity, String> {

    List<NegotiationMessageEntity> findByQuoteIdOrderByCreatedAtAsc(String quoteId);
}

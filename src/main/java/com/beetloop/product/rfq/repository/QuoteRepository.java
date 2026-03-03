package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.QuoteEntity;
import com.beetloop.product.rfq.enums.QuoteStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends MongoRepository<QuoteEntity, String> {

    List<QuoteEntity> findByRfqId(String rfqId);
    List<QuoteEntity> findByRfqIdAndVendorId(String rfqId, String vendorId);
    Optional<QuoteEntity> findFirstByRfqIdAndVendorIdAndStatus(String rfqId, String vendorId, QuoteStatus status);
    List<QuoteEntity> findByRfqIdAndStatus(String rfqId, QuoteStatus status);
    Optional<QuoteEntity> findByRfqInviteIdAndVersion(String rfqInviteId, Integer version);
}

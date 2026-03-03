package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.RfqInviteEntity;
import com.beetloop.product.rfq.enums.RfqInviteStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqInviteRepository extends MongoRepository<RfqInviteEntity, String> {

    Optional<RfqInviteEntity> findByRfqIdAndVendorId(String rfqId, String vendorId);
    List<RfqInviteEntity> findByRfqId(String rfqId);
    List<RfqInviteEntity> findByRfqIdAndStatus(String rfqId, RfqInviteStatus status);
}

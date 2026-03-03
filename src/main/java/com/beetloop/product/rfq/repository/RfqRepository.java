package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.RfqEntity;
import com.beetloop.product.rfq.enums.RfqStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RfqRepository extends MongoRepository<RfqEntity, String> {

    Optional<RfqEntity> findByIdAndBuyerId(String id, String buyerId);
    List<RfqEntity> findByBuyerId(String buyerId);
    List<RfqEntity> findByStatus(RfqStatus status);
}

package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.PurchaseOrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends MongoRepository<PurchaseOrderEntity, String> {

    Optional<PurchaseOrderEntity> findByRfqId(String rfqId);
    Optional<PurchaseOrderEntity> findByPoNumber(String poNumber);
    List<PurchaseOrderEntity> findByBuyerId(String buyerId);
    List<PurchaseOrderEntity> findByVendorId(String vendorId);
}

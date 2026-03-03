package com.beetloop.product.rfq.repository;

import com.beetloop.product.rfq.entity.LeadEntity;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends MongoRepository<LeadEntity, String> {

    Optional<LeadEntity> findByIdAndBuyerId(String id, String buyerId);
}
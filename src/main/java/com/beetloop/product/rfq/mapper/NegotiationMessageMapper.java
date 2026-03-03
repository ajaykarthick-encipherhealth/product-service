package com.beetloop.product.rfq.mapper;

import com.beetloop.product.rfq.dto.NegotiationMessageDTO;
import com.beetloop.product.rfq.entity.NegotiationMessageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NegotiationMessageMapper {

    NegotiationMessageDTO toDto(NegotiationMessageEntity entity);
}

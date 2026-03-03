package com.beetloop.product.rfq.mapper;

import com.beetloop.product.rfq.dto.RfqInviteDTO;
import com.beetloop.product.rfq.entity.RfqInviteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RfqInviteMapper {

    RfqInviteDTO toDto(RfqInviteEntity entity);
}

package com.beetloop.product.rfq.mapper;

import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.entity.QuoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuoteMapper {

    @Mapping(source = "quoteCommercialTerms", target = "quoteCommercialTermsDTO")
    QuoteDTO toDto(QuoteEntity entity);
}

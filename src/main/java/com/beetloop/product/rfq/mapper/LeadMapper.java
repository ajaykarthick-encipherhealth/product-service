package com.beetloop.product.rfq.mapper;

import com.beetloop.product.rfq.dto.BudgetDTO;
import com.beetloop.product.rfq.dto.CommercialTermsDTO;
import com.beetloop.product.rfq.dto.LeadRequestDTO;
import com.beetloop.product.rfq.dto.LeadResponseDTO;
import com.beetloop.product.rfq.dto.QuantityDTO;
import com.beetloop.product.rfq.dto.RFQBasicsDTO;
import com.beetloop.product.rfq.dto.QcDTO;
import com.beetloop.product.rfq.dto.SpecificationDTO;
import com.beetloop.product.rfq.dto.SupplierCriteriaDTO;
import com.beetloop.product.rfq.dto.ThirdPartyLabDTO;
import com.beetloop.product.rfq.entity.LeadEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeadMapper {

    @Mapping(target = "specification", ignore = true)
    LeadEntity toEntity(LeadRequestDTO dto);

    LeadResponseDTO toDto(LeadEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "buyerId", ignore = true)
    @Mapping(target = "basics", ignore = true)
    @Mapping(target = "specification", ignore = true)
    @Mapping(target = "qc", ignore = true)
    @Mapping(target = "commercialTerms", ignore = true)
    @Mapping(target = "supplierCriteria", ignore = true)
    void updateEntityFromDto(@MappingTarget LeadEntity entity, LeadRequestDTO dto);

    @AfterMapping
    default void mergeNested(@MappingTarget LeadEntity entity, LeadRequestDTO dto) {
        if (dto.getBasics() != null) {
            if (entity.getBasics() == null) entity.setBasics(new RFQBasicsDTO());
            updateBasics(entity.getBasics(), dto.getBasics());
        }
        if (dto.getSpecification() != null) {
            if (entity.getSpecification() == null) entity.setSpecification(new SpecificationDTO());
            updateSpecification(entity.getSpecification(), dto.getSpecification());
        }
        if (dto.getQc() != null) {
            if (entity.getQc() == null) entity.setQc(new QcDTO());
            updateQc(entity.getQc(), dto.getQc());
        }
        if (dto.getCommercialTerms() != null) {
            if (entity.getCommercialTerms() == null) entity.setCommercialTerms(new CommercialTermsDTO());
            updateCommercialTerms(entity.getCommercialTerms(), dto.getCommercialTerms());
        }
        if (dto.getSupplierCriteria() != null) {
            if (entity.getSupplierCriteria() == null) entity.setSupplierCriteria(new SupplierCriteriaDTO());
            updateSupplierCriteria(entity.getSupplierCriteria(), dto.getSupplierCriteria());
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBasics(@MappingTarget RFQBasicsDTO target, RFQBasicsDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSpecification(@MappingTarget SpecificationDTO target, SpecificationDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQc(@MappingTarget QcDTO target, QcDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCommercialTerms(@MappingTarget CommercialTermsDTO target, CommercialTermsDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSupplierCriteria(@MappingTarget SupplierCriteriaDTO target, SupplierCriteriaDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateQuantity(@MappingTarget QuantityDTO target, QuantityDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBudget(@MappingTarget BudgetDTO target, BudgetDTO source);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateThirdPartyLab(@MappingTarget ThirdPartyLabDTO target, ThirdPartyLabDTO source);
}
package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class SupplierCriteriaDTO {
    private List<String> preferredSupplierType;
    private List<String> geographyPreference;
    private String minimumYearsofExperience;
    private String minimumAnnualTurnOver;
    private String productionCapacityRequired;
    private Boolean isMustHaveExportExperience;
    private String minimumReliablityScore;
    private List<String> documentsRequiredFromSuppliers;
}
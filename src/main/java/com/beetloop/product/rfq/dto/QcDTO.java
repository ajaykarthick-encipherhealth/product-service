package com.beetloop.product.rfq.dto;

import lombok.Data;

import java.util.List;

@Data
public class QcDTO {
    private List<String> requiredCertifications;
    private List<String> regulatoryCompliance;
    private ThirdPartyLabDTO thirdPartyLab;
    private List<String> mustBeFree;
    private String heavyMetalsLimits;
    private String microbiologyLimits;
}
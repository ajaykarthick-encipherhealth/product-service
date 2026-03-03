package com.beetloop.product.rfq.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.List;

@Data
public class RFQBasicsDTO {
        private String rfQTitle;
        private String primaryCategory;
        private String subCategory;
        private List<String> targetMarkets;
        private String responseTimeline;
        private LocalDate expectedResponseDate;
        private Boolean markAsConfidetial;
        private String additionalContext;
}

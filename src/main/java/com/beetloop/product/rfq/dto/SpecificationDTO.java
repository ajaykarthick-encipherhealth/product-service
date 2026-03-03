package com.beetloop.product.rfq.dto;

import com.beetloop.product.rfq.dto.SpecificationFieldDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpecificationDTO {

    private List<SpecificationFieldDTO> fields = new ArrayList<>();
}
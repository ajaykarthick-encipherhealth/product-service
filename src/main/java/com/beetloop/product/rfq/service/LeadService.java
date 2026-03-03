package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.LeadRequestDTO;
import com.beetloop.product.rfq.dto.LeadResponseDTO;

public interface LeadService {
    LeadResponseDTO saveLead(String buyerId, LeadRequestDTO requestDTO);
    LeadResponseDTO getLeadById(String id);
}
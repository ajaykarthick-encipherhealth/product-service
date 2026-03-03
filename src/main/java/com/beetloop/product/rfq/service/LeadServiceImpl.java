package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.LeadRequestDTO;
import com.beetloop.product.rfq.dto.LeadResponseDTO;
import com.beetloop.product.rfq.entity.LeadEntity;
import com.beetloop.product.rfq.mapper.LeadMapper;
import com.beetloop.product.rfq.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    public LeadServiceImpl(LeadRepository leadRepository, LeadMapper leadMapper) {
        this.leadRepository = leadRepository;
        this.leadMapper = leadMapper;
    }

    @Override
    public LeadResponseDTO saveLead(String buyerId, LeadRequestDTO requestDTO) {
        String id = requestDTO.getId();
        boolean hasId = id != null && !id.isBlank();
        if (!hasId) {
            LeadEntity entity = leadMapper.toEntity(requestDTO);
            entity.setId(null);
            entity.setBuyerId(buyerId);
            LeadEntity saved = leadRepository.save(entity);
            return leadMapper.toDto(saved);
        }
        LeadEntity existing = leadRepository.findByIdAndBuyerId(id, buyerId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id + " for buyer: " + buyerId));
        leadMapper.updateEntityFromDto(existing, requestDTO);
        LeadEntity saved = leadRepository.save(existing);
        return leadMapper.toDto(saved);
    }

    @Override
    public LeadResponseDTO getLeadById(String id) {
        LeadEntity leadEntity = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
        return leadMapper.toDto(leadEntity);
    }
}
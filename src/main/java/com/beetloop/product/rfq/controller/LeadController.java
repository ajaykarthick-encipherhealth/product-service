package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.LeadRequestDTO;
import com.beetloop.product.rfq.dto.LeadResponseDTO;
import com.beetloop.product.rfq.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    /**
     * Create or update by buyer. No id in body → create with buyerId. Id present (rfqLeadId) → update that lead for this buyer.
     */
    @PostMapping("/buyers/{buyerId}/leads")
    public ResponseEntity<LeadResponseDTO> saveLead(
            @PathVariable String buyerId,
            @RequestBody @Validated LeadRequestDTO requestDTO) {
        LeadResponseDTO response = leadService.saveLead(buyerId, requestDTO);
        boolean isCreate = requestDTO.getId() == null || requestDTO.getId().isBlank();
        return ResponseEntity.status(isCreate ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadResponseDTO> getLeadById(@PathVariable String id) {

        LeadResponseDTO response = leadService.getLeadById(id);
        return ResponseEntity.ok(response);
    }

}
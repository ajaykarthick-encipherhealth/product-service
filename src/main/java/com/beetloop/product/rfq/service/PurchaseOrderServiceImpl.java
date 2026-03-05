package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.*;
import com.beetloop.product.rfq.entity.*;
import com.beetloop.product.rfq.enums.POStatus;
import com.beetloop.product.rfq.enums.RfqStatus;
import com.beetloop.product.rfq.exception.QuoteActionException;
import com.beetloop.product.rfq.repository.LeadRepository;
import com.beetloop.product.rfq.repository.PurchaseOrderRepository;
import com.beetloop.product.rfq.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final LeadRepository leadRepository;
    private final QuoteRepository quoteRepository;

    private static final BigDecimal DEFAULT_TAX_PERCENT = new BigDecimal("18");
    private static final List<String> DEFAULT_DOCUMENTS = List.of(
            "Commercial Invoice", "Packing List", "Bill of Lading / Airway Bill",
            "Certificate of Analysis (CoA)", "Certificate of Origin (CoO)", "MSDS / SDS",
            "Product Specification Sheet", "Import Export Code (IEC)", "HS Code Declaration"
    );

    @Override
    @Transactional
    public PurchaseOrderDTO createFromApprovedRfq(String rfqId, String buyerId, CreatePORequestDTO overrides) {
        LeadEntity rfq = leadRepository.findById(rfqId)
                .orElseThrow(() -> new QuoteActionException("RFQ not found: " + rfqId));
        if (!rfq.getBuyerId().equals(buyerId)) {
            throw new QuoteActionException("Only the RFQ buyer can create a PO for this RFQ");
        }
        if (rfq.getStatus() != RfqStatus.AWARDED) {
            throw new QuoteActionException("RFQ must be awarded before creating a PO; current status: " + rfq.getStatus());
        }
        String quoteId = rfq.getSelectedQuoteId();
        if (quoteId == null || quoteId.isBlank()) {
            throw new QuoteActionException("No selected quote for this RFQ");
        }
        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteActionException("Quote not found: " + quoteId));

        String poNumber = generatePoNumber();
        Instant now = Instant.now();
        LocalDate issueDate = LocalDate.now();

        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setPoNumber(poNumber);
        po.setRfqId(rfqId);
        po.setQuoteId(quoteId);
        po.setBuyerId(buyerId);
        po.setVendorId(quote.getVendorId());
        po.setStatus(POStatus.DRAFT);
        po.setCreatedAt(now);
        po.setUpdatedAt(now);
        po.setIssueDate(issueDate);
        po.setValidityDays(30);

        CompanyInfoEmbedded buyerEntity = new CompanyInfoEmbedded();
        buyerEntity.setCompanyName(overrides != null && overrides.getBuyerCompanyName() != null ? overrides.getBuyerCompanyName() : "Buyer");
        buyerEntity.setAddress(overrides != null ? overrides.getBuyerAddress() : null);
        buyerEntity.setGstin(overrides != null ? overrides.getBuyerGstin() : null);
        po.setBuyerEntity(buyerEntity);

        CompanyInfoEmbedded vendorEntity = new CompanyInfoEmbedded();
        vendorEntity.setCompanyName(overrides != null && overrides.getVendorCompanyName() != null ? overrides.getVendorCompanyName() : "Vendor");
        vendorEntity.setAddress(overrides != null ? overrides.getVendorAddress() : null);
        vendorEntity.setGstin(overrides != null ? overrides.getVendorGstin() : null);
        po.setVendorEntity(vendorEntity);

        po.setBillTo(overrides != null ? overrides.getBillTo() : null);
        po.setShipTo(overrides != null ? overrides.getShipTo() : null);

        String paymentTerms = null;
        String currency = "USD";
        if (quote.getQuoteCommercialTerms() != null && quote.getQuoteCommercialTerms().getCommercialTermsPricing() != null) {
            CommercialTermsPricing pricing = quote.getQuoteCommercialTerms().getCommercialTermsPricing();
            paymentTerms = pricing.getPaymentTerms();
            if (pricing.getCurrency() != null) currency = pricing.getCurrency();
        }
        po.setPaymentTerms(paymentTerms != null ? paymentTerms : "Net 30 days from invoice date");
        po.setTaxStructure("GST 18% applicable");
        po.setCurrency(currency);

        List<POLineItemEmbedded> lineItems = buildLineItems(rfq, quote);
        po.setLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(POLineItemEmbedded::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        po.setSubtotal(subtotal);
        BigDecimal taxTotal = subtotal.multiply(DEFAULT_TAX_PERCENT).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        po.setTaxTotal(taxTotal);
        po.setGrandTotal(subtotal.add(taxTotal));

        po.setExpectedDeliveryDate(issueDate.plusWeeks(3));
        po.setDeliveryType("One-time");
        po.setPartialDeliveryAllowed("Not Allowed");

        List<PODocumentRequirementEmbedded> docReqs = new ArrayList<>();
        for (String name : DEFAULT_DOCUMENTS) {
            PODocumentRequirementEmbedded doc = new PODocumentRequirementEmbedded();
            doc.setDocumentName(name);
            doc.setRequiredFor("Customs".equals(name) || name.startsWith("Bill of") ? "Customs & Logistics" : "Customs");
            doc.setMandatory(!name.contains("MSDS"));
            doc.setStatus("PENDING");
            docReqs.add(doc);
        }
        po.setDocumentRequirements(docReqs);

        po = purchaseOrderRepository.save(po);
        return toDto(po);
    }

    private List<POLineItemEmbedded> buildLineItems(LeadEntity rfq, QuoteEntity quote) {
        List<POLineItemEmbedded> items = new ArrayList<>();
        String productName = rfq.getBasics() != null && rfq.getBasics().getRfQTitle() != null
                ? rfq.getBasics().getRfQTitle()
                : "Product";
        String quantityStr = "1";
        String unit = "kg";
        BigDecimal unitPrice = BigDecimal.ZERO;
        if (rfq.getCommercialTerms() != null && rfq.getCommercialTerms().getQuantity() != null) {
            if (rfq.getCommercialTerms().getQuantity().getQuantityRequired() != null) {
                quantityStr = String.valueOf(rfq.getCommercialTerms().getQuantity().getQuantityRequired());
            }
            if (rfq.getCommercialTerms().getQuantity().getUnit() != null) {
                unit = rfq.getCommercialTerms().getQuantity().getUnit();
            }
        }
        if (quote.getQuoteCommercialTerms() != null && quote.getQuoteCommercialTerms().getCommercialTermsPricing() != null) {
            CommercialTermsPricing p = quote.getQuoteCommercialTerms().getCommercialTermsPricing();
            if (p.getUnitPrice() != null) unitPrice = p.getUnitPrice();
            if (p.getQuantity() != null) quantityStr = p.getQuantity();
            if (p.getUnit() != null) unit = p.getUnit();
        }
        BigDecimal qty = new BigDecimal(quantityStr);
        BigDecimal lineTotal = unitPrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        String spec = rfq.getBasics() != null ? rfq.getBasics().getPrimaryCategory() : null;
        String sku = spec != null ? spec : "ITEM-001";

        POLineItemEmbedded line = new POLineItemEmbedded();
        line.setItemDescription(productName);
        line.setSpecificationOrSku(sku);
        line.setQuantity(quantityStr);
        line.setUnit(unit);
        line.setUnitPrice(unitPrice);
        line.setTaxPercent(DEFAULT_TAX_PERCENT);
        line.setLineTotal(lineTotal);
        items.add(line);
        return items;
    }

    private String generatePoNumber() {
        int year = Year.now().getValue();
        long count = purchaseOrderRepository.count();
        return "PO-" + year + "-" + String.format("%04d", count + 1);
    }

    @Override
    public PurchaseOrderDTO getById(String poId) {
        PurchaseOrderEntity po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new QuoteActionException("Purchase order not found: " + poId));
        return toDto(po);
    }

    @Override
    public PurchaseOrderDTO getByRfqId(String rfqId) {
        PurchaseOrderEntity po = purchaseOrderRepository.findByRfqId(rfqId)
                .orElseThrow(() -> new QuoteActionException("Purchase order not found for RFQ: " + rfqId));
        return toDto(po);
    }

    @Override
    public PurchaseOrderDTO preview(String poId) {
        return getById(poId);
    }

    @Override
    @Transactional
    public PurchaseOrderDTO issuePo(String poId, String approvalRefId, String authorizedBy, String authorizedByRole) {
        PurchaseOrderEntity po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new QuoteActionException("Purchase order not found: " + poId));
        po.setStatus(POStatus.ISSUED);
        po.setApprovalRefId(approvalRefId);
        po.setAuthorizedBy(authorizedBy);
        po.setAuthorizedByRole(authorizedByRole);
        po.setAuthorizedAt(Instant.now());
        po.setUpdatedAt(Instant.now());
        po = purchaseOrderRepository.save(po);
        return toDto(po);
    }

    @Override
    public List<PurchaseOrderDTO> listByBuyerId(String buyerId) {
        return purchaseOrderRepository.findByBuyerId(buyerId).stream()
                .map(this::toDto)
                .toList();
    }

    private PurchaseOrderDTO toDto(PurchaseOrderEntity po) {
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(po.getId());
        dto.setPoNumber(po.getPoNumber());
        dto.setRfqId(po.getRfqId());
        dto.setQuoteId(po.getQuoteId());
        dto.setBuyerId(po.getBuyerId());
        dto.setVendorId(po.getVendorId());
        dto.setStatus(po.getStatus());
        if (po.getBuyerEntity() != null) {
            CompanyInfoDTO b = new CompanyInfoDTO();
            b.setCompanyName(po.getBuyerEntity().getCompanyName());
            b.setAddress(po.getBuyerEntity().getAddress());
            b.setGstin(po.getBuyerEntity().getGstin());
            dto.setBuyerEntity(b);
        }
        if (po.getVendorEntity() != null) {
            CompanyInfoDTO v = new CompanyInfoDTO();
            v.setCompanyName(po.getVendorEntity().getCompanyName());
            v.setAddress(po.getVendorEntity().getAddress());
            v.setGstin(po.getVendorEntity().getGstin());
            dto.setVendorEntity(v);
        }
        dto.setBillTo(po.getBillTo());
        dto.setShipTo(po.getShipTo());
        dto.setPaymentTerms(po.getPaymentTerms());
        dto.setTaxStructure(po.getTaxStructure());
        dto.setSubtotal(po.getSubtotal());
        dto.setTaxTotal(po.getTaxTotal());
        dto.setGrandTotal(po.getGrandTotal());
        dto.setCurrency(po.getCurrency());
        dto.setExpectedDeliveryDate(po.getExpectedDeliveryDate());
        dto.setDeliveryType(po.getDeliveryType());
        dto.setPartialDeliveryAllowed(po.getPartialDeliveryAllowed());
        dto.setAuthorizedBy(po.getAuthorizedBy());
        dto.setAuthorizedByRole(po.getAuthorizedByRole());
        dto.setApprovalRefId(po.getApprovalRefId());
        dto.setAuthorizedAt(po.getAuthorizedAt());
        dto.setIssueDate(po.getIssueDate());
        dto.setValidityDays(po.getValidityDays());
        dto.setCreatedAt(po.getCreatedAt());
        dto.setUpdatedAt(po.getUpdatedAt());
        if (po.getLineItems() != null) {
            dto.setLineItems(po.getLineItems().stream().map(this::lineToDto).toList());
        }
        if (po.getDocumentRequirements() != null) {
            dto.setDocumentRequirements(po.getDocumentRequirements().stream().map(this::docToDto).toList());
        }
        return dto;
    }

    private POLineItemDTO lineToDto(POLineItemEmbedded L) {
        POLineItemDTO dto = new POLineItemDTO();
        dto.setItemDescription(L.getItemDescription());
        dto.setSpecificationOrSku(L.getSpecificationOrSku());
        dto.setQuantity(L.getQuantity());
        dto.setUnit(L.getUnit());
        dto.setUnitPrice(L.getUnitPrice());
        dto.setTaxPercent(L.getTaxPercent());
        dto.setLineTotal(L.getLineTotal());
        return dto;
    }

    private PODocumentRequirementDTO docToDto(PODocumentRequirementEmbedded d) {
        PODocumentRequirementDTO dto = new PODocumentRequirementDTO();
        dto.setDocumentName(d.getDocumentName());
        dto.setRequiredFor(d.getRequiredFor());
        dto.setMandatory(d.isMandatory());
        dto.setStatus(d.getStatus());
        return dto;
    }
}

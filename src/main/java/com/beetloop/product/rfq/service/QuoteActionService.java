package com.beetloop.product.rfq.service;

import com.beetloop.product.rfq.dto.NegotiationMessageDTO;
import com.beetloop.product.rfq.dto.QuoteDTO;
import com.beetloop.product.rfq.dto.QuoteRequestDTO;
import com.beetloop.product.rfq.enums.NegotiationRole;

import java.util.List;

public interface QuoteActionService {

    QuoteDTO createQuote(String vendorId, String rfqId, QuoteRequestDTO request);

    QuoteDTO submitQuote(String quoteId, String vendorId);

    QuoteDTO patchQuote(String quoteId, String vendorId, QuoteRequestDTO request);

    QuoteDTO getQuote(String quoteId);

    QuoteDTO acceptQuote(String quoteId, String buyerId);

    QuoteDTO rejectQuote(String quoteId, String reason, String buyerId);

    NegotiationMessageDTO negotiateQuote(String quoteId, String message, String authorId, NegotiationRole role);

    QuoteDTO submitRevisedQuote(String previousQuoteId, String vendorId, QuoteRequestDTO request);

    List<NegotiationMessageDTO> getNegotiationMessages(String quoteId);
}

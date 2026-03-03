package com.beetloop.product.rfq.controller;

import com.beetloop.product.rfq.dto.NegotiationMessageDTO;
import com.beetloop.product.rfq.service.QuoteActionService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class NegotiationWebSocketController {


    private final QuoteActionService negotiationService;
    private final SimpMessagingTemplate messagingTemplate;

    public NegotiationWebSocketController(
            QuoteActionService negotiationService,
            SimpMessagingTemplate messagingTemplate) {
        this.negotiationService = negotiationService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Client sends to: /app/negotiation.send
     */
    @MessageMapping("/negotiation.send")
    public void handleNegotiationMessage(NegotiationMessageDTO request) {

        NegotiationMessageDTO savedMessage =
                negotiationService.negotiateQuote(
                        request.getQuoteId(),
                        request.getMessage(),
                        request.getAuthorId(),
                        request.getRole()
                );

        /**
         * Broadcast to all users watching this quote
         * Topic: /topic/negotiation/{quoteId}
         */
        messagingTemplate.convertAndSend(
                "/topic/negotiation/" + request.getQuoteId(),
                savedMessage
        );
    }
}
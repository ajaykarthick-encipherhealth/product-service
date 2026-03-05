# RFQ → Quote → Approval → Purchase Order Flow

End-to-end flow from vendor quotes through internal approvals to PO creation.

---

## High-level flow diagram

```mermaid
flowchart LR
    subgraph Comparison["Vendor Quote Comparison"]
        A[Multiple vendor quotes] --> B[Buyer selects one vendor]
    end
    subgraph Approval["Internal Approvals"]
        B --> C[Approval chain created]
        C --> D[Step 1: R&D Head]
        D --> E[Step 2: QC Manager]
        E --> F[Step 3: Finance Director]
        F --> G[All approved]
    end
    subgraph Finalize["Finalize & PO"]
        G --> H[Buyer finalizes order]
        H --> I[Quote ACCEPTED, RFQ AWARDED]
        I --> J[Create Purchase Order]
        J --> K[Preview / Issue PO]
    end

    A ~~~ B ~~~ C ~~~ D ~~~ E ~~~ F ~~~ G ~~~ H ~~~ I ~~~ J ~~~ K
```

---

## Detailed sequence (API + state)

```mermaid
sequenceDiagram
    participant Buyer
    participant UI as UI (Comparison)
    participant API as Product Service API
    participant DB as MongoDB

    Note over Buyer,DB: 1. Vendor Quote Comparison
    Buyer->>UI: View comparison screen
    UI->>API: GET /api/v1/quotes?rfqId={rfqId}
    API->>DB: findByRfqId
    DB-->>API: List<Quote>
    API-->>UI: Quote list
    Buyer->>UI: Click "Select Vendor" on one quote

    Note over Buyer,DB: 2. Select quote for approval
    UI->>API: POST /api/v1/quotes/{quoteId}/select-for-approval
    Note right of API: Body: ApprovalChainRequestDTO (steps)
    API->>API: quote → SELECTED, RFQ → PENDING_APPROVAL
    API->>API: Lead.selectedQuoteId = quoteId
    API->>API: Create approval chain
    API->>DB: Save quote, lead, approval chain
    API-->>UI: QuoteDTO
    UI->>Buyer: Navigate to Internal Approvals

    Note over Buyer,DB: 3. Internal approvals (sequential)
    loop Each approver
        Buyer->>UI: Approver opens approvals
        UI->>API: GET /api/v1/approvals/rfq/{rfqId}
        API-->>UI: ApprovalChainDTO (steps, status)
        Buyer->>UI: Approver submits approve/reject
        UI->>API: POST .../chains/{chainId}/steps/{stepOrder}/submit
        API->>DB: Update step status, chain status
        API-->>UI: ApprovalChainDTO
    end

    Note over Buyer,DB: 4. Finalize order (all steps approved)
    Buyer->>UI: Click "Finalize Order"
    UI->>API: POST /api/v1/approvals/rfq/{rfqId}/finalize
    API->>API: Check isFullyApproved(rfqId)
    API->>API: acceptQuote(selectedQuoteId) → ACCEPTED, AWARDED
    API->>DB: Update quote, RFQ, invites
    API-->>UI: QuoteDTO (ACCEPTED)
    UI->>Buyer: Navigate to Finalize / Create PO

    Note over Buyer,DB: 5. Create Purchase Order
    Buyer->>UI: Review summary, click "Create Purchase Order"
    UI->>API: POST /api/v1/purchase-orders/from-rfq/{rfqId}
    API->>API: Build PO from RFQ + selected quote
    API->>API: Generate PO number (e.g. PO-2026-0001)
    API->>DB: Save PurchaseOrder (DRAFT)
    API-->>UI: PurchaseOrderDTO
    UI->>Buyer: Show PO Generation screen

    Note over Buyer,DB: 6. Preview & issue PO
    UI->>API: GET /api/v1/purchase-orders/{poId}/preview
    API-->>UI: PO data (for PDF / display)
    Buyer->>UI: Confirm and issue
    UI->>API: POST /api/v1/purchase-orders/{poId}/issue
    API->>DB: status = ISSUED, set authorizedBy, approvalRefId
    API-->>UI: PurchaseOrderDTO (ISSUED)
```

---

## State transitions

### Quote status

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Vendor creates
    DRAFT --> SUBMITTED: Vendor submits
    SUBMITTED --> SELECTED: Buyer selects for approval
    SUBMITTED --> UNDER_NEGOTIATION: Negotiation started
    SUBMITTED --> REJECTED: Buyer rejects
    UNDER_NEGOTIATION --> SUBMITTED: Revised quote
    UNDER_NEGOTIATION --> SELECTED: Buyer selects for approval
    UNDER_NEGOTIATION --> REJECTED: Buyer rejects
    SELECTED --> ACCEPTED: All approvals done + Finalize
    ACCEPTED --> [*]
    REJECTED --> [*]
```

### RFQ (Lead) status

```mermaid
stateDiagram-v2
    [*] --> DRAFT: Buyer creates lead
    DRAFT --> OPEN: Published / invites sent
    OPEN --> PENDING_APPROVAL: Buyer selects a quote
    PENDING_APPROVAL --> AWARDED: Finalize order (all approved)
    PENDING_APPROVAL --> OPEN: (if selection changed – not in API yet)
    AWARDED --> [*]: PO can be created
    OPEN --> CLOSED: Closed without award
```

### Approval chain

```mermaid
flowchart LR
    PENDING --> APPROVED: All steps approved
    PENDING --> REJECTED: Any step rejected
```

---

## API endpoints summary

| Step | Method | Endpoint | Purpose |
|------|--------|----------|---------|
| Comparison | GET | `/api/v1/quotes?rfqId={rfqId}` | List quotes for RFQ |
| Select | POST | `/api/v1/quotes/{quoteId}/select-for-approval` | Select quote, create approval chain |
| Approvals | GET | `/api/v1/approvals/rfq/{rfqId}` | Get approval chain |
| Approve | POST | `/api/v1/approvals/chains/{chainId}/steps/{stepOrder}/submit` | Submit one step (X-Approver-Id) |
| Finalize | POST | `/api/v1/approvals/rfq/{rfqId}/finalize` | Finalize order (X-Buyer-Id) |
| Create PO | POST | `/api/v1/purchase-orders/from-rfq/{rfqId}` | Create PO from awarded RFQ |
| Preview PO | GET | `/api/v1/purchase-orders/{poId}/preview` | Get PO for PDF/display |
| Issue PO | POST | `/api/v1/purchase-orders/{poId}/issue` | DRAFT → ISSUED |

---

## Key entities

- **Lead (RFQ)** – `selectedQuoteId` holds the quote chosen for approval and used for PO creation.
- **Quote** – `SELECTED` = chosen for approval; `ACCEPTED` only after finalize.
- **ApprovalChain** – One per RFQ when a quote is selected; steps are sequential.
- **PurchaseOrder** – Created from awarded RFQ using `selectedQuoteId` and quote/RFQ data.

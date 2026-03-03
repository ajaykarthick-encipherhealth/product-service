# RFQ Procurement – Schema & Behaviour

## 1. Database schemas (MongoDB)

### rfqs
| Field | Type | Description |
|-------|------|-------------|
| id | String | PK |
| buyerId | String | Owner (indexed) |
| status | RfqStatus | DRAFT, OPEN, AWARDED, CLOSED |
| basics, specification, qc, commercialTerms, supplierCriteria | embedded | RFQ content (DTOs) |
| createdAt, updatedAt | Instant | Audit |
| version | Long | Optimistic lock |

### rfq_invites
| Field | Type | Description |
|-------|------|-------------|
| id | String | PK |
| rfqId | String | FK to rfqs |
| vendorId | String | Unique per RFQ (compound index rfqId + vendorId) |
| status | RfqInviteStatus | INVITED, RESPONDED, REJECTED, AWARDED |
| latestQuoteId | String | Current/latest quote for this invite |
| invitedAt, respondedAt, updatedAt | Instant | Audit |

### quotes
| Field | Type | Description |
|-------|------|-------------|
| id | String | PK |
| rfqId, vendorId, rfqInviteId | String | References (indexed) |
| status | QuoteStatus | SUBMITTED, UNDER_NEGOTIATION, ACCEPTED, REJECTED |
| previousQuoteId | String | When revised, link to prior quote (history preserved) |
| version | Integer | 1 = initial, 2+ = revision |
| totalPrice, currency, lineItems, notes | - | Commercial terms |
| submittedAt, updatedAt | Instant | Audit |
| documentVersion | Long | Optimistic lock |

### negotiations
| Field | Type | Description |
|-------|------|-------------|
| id | String | PK |
| quoteId, rfqId | String | References (indexed) |
| role | NegotiationRole | BUYER, VENDOR |
| authorId | String | User who sent the message |
| message | String | Content |
| createdAt | Instant | Audit |

---

## 2. Status enums and transitions

- **RfqStatus**: DRAFT → OPEN → AWARDED | CLOSED  
- **RfqInviteStatus**: INVITED → RESPONDED | REJECTED | AWARDED  
- **QuoteStatus**: SUBMITTED → ACCEPTED | REJECTED | UNDER_NEGOTIATION; UNDER_NEGOTIATION → ACCEPTED | REJECTED or new revised quote  

Revised quotes are new documents with `previousQuoteId`; previous quote is never overwritten.

### Permission rules (buyer vs vendor)

| Actor | Allowed actions |
|-------|-----------------|
| **Buyer** (RFQ owner, `buyerId`) | Invite vendors, accept quote, reject quote, add negotiation messages (as BUYER), view invites and negotiations for their RFQ. |
| **Vendor** (invited vendor, `vendorId`) | Submit initial quote (when invite status = INVITED), submit revised quote (when current quote is UNDER_NEGOTIATION), add negotiation messages (as VENDOR), view negotiations for their quote. |

- Accept and reject may only be performed by the RFQ buyer; identity is enforced via `X-Buyer-Id` and checked against `rfqs.buyerId`.
- Negotiation messages require `X-Author-Id` and `X-Role`: buyer must be `rfqs.buyerId`, vendor must be the quote’s `vendorId`.
- Revised quote may only be submitted by the vendor who owns the previous quote (`previousQuoteId` → quote’s `vendorId`).

---

## 3. APIs and permissions

### RFQ invites (one document per vendor per RFQ in rfq_invites)

| Action | Method | Endpoint | Who | Headers |
|--------|--------|----------|-----|--------|
| Invite vendors | POST | /api/v1/rfqs/{rfqId}/invites | Buyer | X-Buyer-Id, body: `{ "vendorIds": ["v1","v2"] }` |
| List invites for RFQ | GET | /api/v1/rfqs/{rfqId}/invites | - | - |
| Get invite by id | GET | /api/v1/rfqs/invites/{inviteId} | - | - |

- **Invite vendors**: Only RFQ buyer; RFQ must exist and not be AWARDED. Creates one `rfq_invites` document per vendor (skips if already invited). If RFQ was DRAFT, sets status to OPEN.

### Quote actions

| Action | Method | Endpoint | Who | Headers |
|--------|--------|----------|-----|--------|
| Accept quote | POST | /api/v1/quotes/{quoteId}/accept | Buyer | X-Buyer-Id |
| Reject quote | POST | /api/v1/quotes/{quoteId}/reject | Buyer | X-Buyer-Id |
| Negotiate (add message) | POST | /api/v1/quotes/{quoteId}/negotiate | Buyer or Vendor | X-Author-Id, X-Role (BUYER\|VENDOR) |
| Submit revised quote | POST | /api/v1/quotes/{previousQuoteId}/revised | Vendor | X-Vendor-Id |
| Get negotiation thread | GET | /api/v1/quotes/{quoteId}/negotiations | Buyer or Vendor | - |

- **Accept**: Only RFQ buyer; quote must be SUBMITTED or UNDER_NEGOTIATION; RFQ must not be AWARDED.  
- **Reject**: Only RFQ buyer; quote must not be ACCEPTED.  
- **Negotiate**: Buyer = RFQ owner; Vendor = quote’s vendor. Quote must be SUBMITTED or UNDER_NEGOTIATION.  
- **Revised quote**: Only vendor of the previous quote; previous quote must be UNDER_NEGOTIATION.

---

## 4. Behaviour summary

- **Invite vendors**: For each vendorId, create one document in `rfq_invites` with status INVITED (idempotent per rfqId+vendorId). Optionally move RFQ from DRAFT to OPEN.
- **Accept quote**: Quote → ACCEPTED; other quotes for same RFQ → REJECTED; RFQ → AWARDED; winning invite → AWARDED; other invites → REJECTED.  
- **Reject quote**: Quote → REJECTED; its invite → REJECTED; RFQ unchanged.  
- **Negotiate**: Quote → UNDER_NEGOTIATION (if not already); new message stored in `negotiations`.  
- **Submit revised quote**: New quote document with `previousQuoteId`, `version` incremented; invite’s `latestQuoteId` updated; history preserved.

---

## 5. Audit and scale

- **Timestamps**: `createdAt`/`updatedAt` on rfqs; `invitedAt`, `respondedAt`, `updatedAt` on invites; `submittedAt`/`updatedAt` on quotes; `createdAt` on negotiations.
- **Optimistic locking**: `version` on rfqs, `documentVersion` on quotes.
- **History**: Negotiations are append-only; quote history preserved via `previousQuoteId` chain (no overwrite).
- **Indexes**: `rfqId`, `vendorId`, `quoteId` on quotes and negotiations; `buyerId` on rfqs; compound unique `(rfqId, vendorId)` on rfq_invites.

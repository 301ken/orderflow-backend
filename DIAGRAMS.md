# OrderFlow — Architecture & Design Diagrams

> **To export for slides:** open https://mermaid.live → paste one ```mermaid``` block → menu **Actions → SVG** (crisp at any size) or PNG.
> Consistent palette: 🟣 client · 🔴 security · 🔵 web · 🟢 service · 🟠 repository · 🟪 database · ⚪ external.

---
## 1. System Context

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, Helvetica, sans-serif','fontSize':'15px','lineColor':'#94a3b8'}}}%%
flowchart LR
    Client([👤 Client / Frontend]):::client
    subgraph APP[" "]
        direction TB
        APPTITLE["🏦 <b>OrderFlow</b><br/><i>Spring Boot REST Service</i>"]:::apptitle
        API["REST API<br/>(JWT-secured)"]:::web
    end
    DB[("🗄️ PostgreSQL / H2")]:::db
    Stripe["💳 Stripe API"]:::ext
    Firebase["🔥 Firebase Auth"]:::ext
    S3["📦 AWS S3"]:::ext

    Client -->|HTTPS + Bearer JWT| API
    API -->|JPA| DB
    API -->|create PaymentIntent| Stripe
    Stripe -. "webhook: payment result" .-> API
    API -->|verify ID token| Firebase
    API -->|store / fetch documents| S3

    classDef client fill:#ede9fe,stroke:#7c3aed,stroke-width:2px,color:#4c1d95,font-weight:bold;
    classDef web fill:#dbeafe,stroke:#2563eb,stroke-width:2px,color:#1e3a8a,font-weight:bold;
    classDef db fill:#f5f3ff,stroke:#7c3aed,stroke-width:2px,color:#4c1d95;
    classDef ext fill:#f8fafc,stroke:#64748b,stroke-width:1.5px,color:#334155;
    classDef apptitle fill:#eff6ff,stroke:#eff6ff,color:#1e3a8a;
    style APP fill:#eff6ff,stroke:#2563eb,stroke-width:2px,stroke-dasharray:4 3;
```

---
## 2. Layered Architecture

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, Helvetica, sans-serif','fontSize':'14px','lineColor':'#94a3b8'}}}%%
flowchart TB
    Client([👤 Client]):::client
    subgraph SEC["🔴 Security Filter Chain"]
        JWT["JwtAuthenticationFilter"]:::sec
    end
    subgraph WEB["🔵 Web Layer — Controllers"]
        direction LR
        OC[OrderController]:::web
        PC[PaymentController]:::web
        PRC[ProductController]:::web
        DC[DocumentController]:::web
    end
    subgraph SVC["🟢 Service Layer — Business Logic"]
        direction LR
        OS[OrderService]:::svc
        SM[OrderStateMachine]:::svc
        PS[ProductService]:::svc
        AS[AuditService]:::svc
        PAY[PaymentService]:::svc
    end
    subgraph REPO["🟠 Repository Layer — Spring Data JPA"]
        direction LR
        OR[OrderRepository]:::repo
        PR[ProductRepository]:::repo
        AR[AuditLogRepository]:::repo
    end
    DB[("🗄️ Database")]:::db

    Client --> JWT --> WEB
    OC --> OS
    PC --> PAY
    PC --> OS
    OS --> SM
    OS --> PS
    OS --> AS
    OS --> OR
    PS --> PR
    AS --> AR
    REPO --> DB

    classDef client fill:#ede9fe,stroke:#7c3aed,stroke-width:2px,color:#4c1d95,font-weight:bold;
    classDef sec fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#7f1d1d;
    classDef web fill:#dbeafe,stroke:#2563eb,stroke-width:2px,color:#1e3a8a;
    classDef svc fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d;
    classDef repo fill:#fef3c7,stroke:#d97706,stroke-width:2px,color:#78350f;
    classDef db fill:#f5f3ff,stroke:#7c3aed,stroke-width:2px,color:#4c1d95;
    style SEC fill:#fef2f2,stroke:#dc2626,stroke-width:1.5px;
    style WEB fill:#eff6ff,stroke:#2563eb,stroke-width:1.5px;
    style SVC fill:#f0fdf4,stroke:#16a34a,stroke-width:1.5px;
    style REPO fill:#fffbeb,stroke:#d97706,stroke-width:1.5px;
```

---
## 3. Domain Model (ER)

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, sans-serif','fontSize':'14px'}}}%%
erDiagram
    CLIENT ||--o{ ORDER : places
    ORDER  ||--o{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : "appears in"
    CLIENT {
        Long id PK
        String name
        String email
    }
    ORDER {
        Long id PK
        double total_price
        OrderStatus status
        Long client_id FK
    }
    ORDER_ITEM {
        Long id PK
        int quantity
        Double unit_price "💡 snapshot at purchase"
        Double sub_total
        Long product_id FK
        Long order_id FK
    }
    PRODUCT {
        Long id PK
        String name
        Double price
    }
    AUDIT_LOG {
        Long id PK
        Instant timestamp
        String actor
        String action
        String entity_type
        Long entity_id
    }
```

---
## 4. Order State Machine ⭐ (showpiece)

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, sans-serif','fontSize':'15px','lineColor':'#64748b'}}}%%
stateDiagram-v2
    direction LR
    [*] --> CREATED
    CREATED --> PENDING_PAYMENT
    PENDING_PAYMENT --> PAID
    PAID --> PROCESSING
    PROCESSING --> COMPLETED
    CREATED --> CANCELLED
    PENDING_PAYMENT --> CANCELLED
    PAID --> CANCELLED
    PENDING_PAYMENT --> FAILED
    PROCESSING --> FAILED
    COMPLETED --> [*]
    CANCELLED --> [*]
    FAILED --> [*]

    classDef happy fill:#dcfce7,stroke:#16a34a,stroke-width:2px,color:#14532d;
    classDef bad fill:#fee2e2,stroke:#dc2626,stroke-width:2px,color:#7f1d1d;
    classDef done fill:#dbeafe,stroke:#2563eb,stroke-width:2px,color:#1e3a8a;
    class CREATED,PENDING_PAYMENT,PAID,PROCESSING happy
    class CANCELLED,FAILED bad
    class COMPLETED done
```

---
## 5. Create-Order Request Lifecycle (sequence)

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, sans-serif','fontSize':'14px','actorBkg':'#dbeafe','actorBorder':'#2563eb','actorTextColor':'#1e3a8a','noteBkgColor':'#fef9c3','noteBorderColor':'#ca8a04'}}}%%
sequenceDiagram
    autonumber
    participant C as 👤 Client
    participant F as 🔴 JwtFilter
    participant Ctrl as 🔵 OrderController
    participant Svc as 🟢 OrderService
    participant Prod as 🟢 ProductService
    participant Repo as 🟠 OrderRepository
    participant Aud as 🟢 AuditService

    C->>F: POST /orders (Bearer JWT)
    F->>F: validate token → SecurityContext
    F->>Ctrl: forward
    Ctrl->>Ctrl: @Valid body
    Ctrl->>Svc: createOrder(order)
    rect rgb(240, 253, 244)
    Note over Svc,Aud: @Transactional (atomic)
    loop each item
        Svc->>Prod: getProductById(id)
        Prod-->>Svc: product (authoritative price)
    end
    Svc->>Svc: total = Σ subtotals · status = CREATED
    Svc->>Repo: save(order)
    Svc->>Aud: record("ORDER_CREATED")
    end
    Svc-->>Ctrl: saved order
    Ctrl-->>C: 201 Created
```

---
## 6. Payment Flow — async + webhook signature verification ⭐

```mermaid
%%{init: {'theme':'base','themeVariables':{'fontFamily':'Segoe UI, sans-serif','fontSize':'14px','actorBkg':'#dbeafe','actorBorder':'#2563eb','actorTextColor':'#1e3a8a','noteBkgColor':'#fef9c3','noteBorderColor':'#ca8a04'}}}%%
sequenceDiagram
    autonumber
    participant C as 👤 Client
    participant PC as 🔵 PaymentController
    participant OS as 🟢 OrderService
    participant S as 💳 Stripe

    C->>PC: POST /orders/{id}/pay
    PC->>S: createPaymentIntent(order)
    S-->>PC: clientSecret
    PC->>OS: changeStatus(PENDING_PAYMENT)
    PC-->>C: PaymentResult
    Note over C,S: customer pays on Stripe ⏳
    S->>PC: POST /webhooks/stripe (signed)
    rect rgb(254, 242, 242)
    PC->>PC: Webhook.constructEvent(payload, sig, secret)
    end
    alt ✅ signature valid + succeeded
        PC->>OS: changeStatus(PAID)
        PC-->>S: 200 processed
    else ❌ invalid signature
        PC-->>S: 400 rejected
    end
```

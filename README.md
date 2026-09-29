# AI Memory API

> **Persistent memory as a service for LLM applications.**
> Plug memory into any chatbot with a simple REST API.

[![CI](https://github.com/PedroNagatomo/ai-memory-api/actions/workflows/ci.yml/badge.svg)](https://github.com/PedroNagatomo/ai-memory-api/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)

---

## What is this?

**AI Memory API** is a multi-tenant REST API that gives your LLM applications **persistent, searchable memory**.

Your chatbot forgets the user between sessions. This API remembers.

```text
User: "Hi, I'm Pedro"
[2 weeks later]
User: "What's my name?"
Bot (with AI Memory): "Your name is Pedro."
Bot (without):        "I don't know your name."
```

###  Features

-  **Automatic fact extraction** — send a conversation, AI extracts durable facts
-  **Full-text search** — find memories by meaning, not just keywords
-  **Multi-tenant** — one API, many apps, fully isolated
-  **API key auth** — simple, secure, `amk_test_...` / `amk_live_...`
-  **Usage tracking** — per-tenant quotas and rate limits built in
-  **Fast** — powered by Groq (ultra-fast LLM inference)
-  **Docker-ready** — one command to run everything
-  **TypeScript SDK** — official client in `sdk/typescript/`
-  **OpenAPI/Swagger** — interactive docs at `/swagger-ui.html`

---

##  Quickstart

### 1. Clone and configure

```bash
git clone https://github.com/PedroNagatomo/ai-memory-api.git
cd ai-memory-api
cp .env.example .env
```

Edit `.env` and add your [Groq API key](https://console.groq.com/keys) (free tier available):

```bash
GROQ_API_KEY=gsk_your_key_here
```

### 2. Run everything with Docker

```bash
docker compose up --build -d
```

That's it. Now:

- **API:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Adminer (DB UI):** http://localhost:8082

### 3. Create your first tenant

```bash
curl -X POST http://localhost:8081/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"My App","email":"me@example.com"}'
```

Response:

```json
{
  "tenantId": "0c30a780-...",
  "apiKey": "amk_test_twdj6teff...",
  "plan": "FREE"
}
```

** Save the API key — it's shown only once.**

### 4. Ingest a conversation

```bash
export API_KEY="amk_test_..."

# Create an end-user (your app's user)
curl -X POST http://localhost:8081/v1/end-users \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"externalId":"user_001","metadata":{"name":"Pedro"}}'

# Ingest a conversation — AI extracts facts automatically
curl -X POST http://localhost:8081/v1/memories/ingest \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "endUserId": "<uuid-from-previous-response>",
    "messages": [
      {"role": "user", "content": "Hi, I am Pedro and I love coffee"},
      {"role": "assistant", "content": "Nice to meet you, Pedro!"}
    ]
  }'
```

Response:

```json
{
  "extracted": 2,
  "persisted": 2,
  "tokensUsed": 978,
  "memories": [
    {"content": "User is named Pedro", "category": "FACT", "importance": 10},
    {"content": "User loves coffee", "category": "PREFERENCE", "importance": 7}
  ]
}
```

### 5. Search memories

```bash
curl -X POST http://localhost:8081/v1/memories/search \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"endUserId":"<uuid>","query":"coffee"}'
```

---

##  TypeScript SDK

```bash
cd sdk/typescript
npm install
```

```typescript
import { AIMemoryClient } from 'aimemory-sdk';

const client = new AIMemoryClient({
  apiKey: process.env.AIMEMORY_API_KEY!,
  baseUrl: 'http://localhost:8081',
});

// Create user
const user = await client.endUsers.create({
  externalId: 'user_001',
  metadata: { name: 'Pedro' },
});

// Ingest — AI extracts facts
const result = await client.memories.ingest({
  endUserId: user.id,
  messages: [
    { role: 'user', content: 'Hi, I am Pedro and I love coffee' },
  ],
});

console.log(result.memories);
// [{ content: 'User is named Pedro', category: 'FACT', importance: 10 }, ...]

// Search
const search = await client.memories.search({
  endUserId: user.id,
  query: 'coffee',
});

// Check usage
const usage = await client.usage.get();
console.log(`${usage.memoriesUsed}/${usage.memoriesQuota} memories used`);
```
---

##  Architecture

```text
┌────────────────────────────────────────────────────────┐
│                    AI Memory API                        │
│                                                         │
│  ┌──────────┐  ┌────────────┐  ┌───────────────────┐  │
│  │ API Key  │→ │ Rate Limit │→ │ Quota Enforcement │  │
│  │ Auth     │  │ (Bucket4j) │  │ (per plan)        │  │
│  └──────────┘  └────────────┘  └───────────────────┘  │
│         │                                               │
│  ┌──────▼─────────────────────────────────────────┐   │
│  │              Service Layer                      │   │
│  │  ┌──────────┐ ┌──────────┐ ┌────────────────┐  │   │
│  │  │ Ingest   │ │ Extract  │ │ Search         │  │   │
│  │  │ (save)   │ │ (Groq)   │ │ (Postgres FTS) │  │   │
│  │  └──────────┘ └──────────┘ └────────────────┘  │   │
│  └────────────────────┬───────────────────────────┘   │
│                       │                                │
│  ┌────────────────────▼───────────────────────────┐   │
│  │  PostgreSQL 16 + pgvector                      │   │
│  │  - tenants, end_users, memories, tenant_usage  │   │
│  │  - Full-text search (tsvector + GIN)           │   │
│  └────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────┘
```

### Design decisions

| Decision | Why |
|----------|-----|
| **Multi-tenant from day 1** | One DB, full isolation via `tenant_id` |
| **API key with SHA-256 hash** | BCrypt is too slow for API keys (sent every request) |
| **Groq for LLM** | Ultra-fast inference, generous free tier |
| **Postgres full-text search** | Native, fast, zero external dependencies |
| **Bucket4j in-memory rate limit** | No Redis dependency for MVP |
| **Soft delete everywhere** | Audit trail + LGPD compliance |
| **Monthly usage aggregation** | `tenant_usage` per `YYYY-MM` — simple billing later |

---

## 📚 API Reference

Full interactive docs: **http://localhost:8081/swagger-ui.html**

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/v1/auth/register` | Register a tenant, get API key |
| `POST` | `/v1/end-users` | Create or get an end-user (idempotent) |
| `GET`  | `/v1/end-users` | List end-users |
| `POST` | `/v1/memories` | Create memory manually |
| `GET`  | `/v1/memories` | List memories (paginated) |
| `GET`  | `/v1/memories/{id}` | Get memory by ID |
| `DELETE` | `/v1/memories/{id}` | Soft delete |
| `POST` | `/v1/memories/ingest` | **AI extracts memories from conversation** |
| `POST` | `/v1/memories/search` | Full-text search with ranking |
| `GET`  | `/v1/usage` | Current month usage + quotas |

### Authentication

Every endpoint (except `/health` and `/v1/auth/register`) requires:

```http
Authorization: Bearer amk_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Error codes

| Status | Meaning |
|--------|---------|
| `400` | Validation error |
| `401` | Invalid or missing API key |
| `402` | Quota exceeded (plan limit reached) |
| `403` | Access denied (wrong tenant) |
| `404` | Resource not found |
| `429` | Rate limit exceeded |
| `502` | Upstream AI provider error |

---

## 💰 Plans

| Plan | Memories/month | AI tokens/month | Rate limit |
|------|---------------|-----------------|------------|
| **FREE** | 1,000 | 50,000 | 100 req/min |
| **PRO** | 50,000 | 1,000,000 | 500 req/min |
| **BUSINESS** | 500,000 | 10,000,000 | 2,000 req/min |

*(Pricing TBD. Plans are enforced but upgrade flow is manual for now.)*

---

## 🛠️ Tech Stack

### Backend

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security + API keys (SHA-256) |
| Database | PostgreSQL 16 + pgvector |
| Migrations | Flyway (V1–V4) |
| AI Provider | Groq (`openai/gpt-oss-20b`) |
| Rate limiting | Bucket4j (in-memory) |
| Documentation | Springdoc OpenAPI |
| Build | Maven |
| Testing | JUnit 5 + Testcontainers |

### SDK

| Component | Technology |
|-----------|-----------|
| Language | TypeScript 5 |
| HTTP | `fetch` (native, Node 18+) |
| Build | `tsc` |
| Testing | Vitest |
| Dependencies | **Zero runtime dependencies** |

### DevOps

| Component | Technology |
|-----------|-----------|
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| DB UI (dev) | Adminer |

---

## 🧪 Testing

### Backend

```bash
cd backend
./mvnw test
```

Covers:
- Unit tests: API key generation, JSON parsing, plan limits
- Integration tests (Testcontainers): repositories, services, multi-tenant isolation

### SDK

```bash
cd sdk/typescript
npm test
```

Covers:
- HTTP client, retry logic, error handling
- Resources: memories, end-users, usage

### End-to-end (manual)

See the [Quickstart](#-quickstart) section.

---

## 📂 Project Structure

```text
ai-memory-api/
├── backend/                           # Spring Boot API
│   ├── src/main/java/com/aimemory/
│   │   ├── ai/                        # AI provider abstraction
│   │   │   ├── groq/                  # Groq client
│   │   │   ├── extraction/            # Conversation → memories
│   │   │   └── model/                 # Domain models
│   │   ├── config/                    # Spring configs
│   │   ├── controller/                # REST controllers
│   │   ├── dto/                       # Request/response DTOs
│   │   ├── entity/                    # JPA entities
│   │   ├── exception/                 # Global exception handling
│   │   ├── repository/                # Spring Data repositories
│   │   ├── security/                  # API key auth + rate limit
│   │   └── service/                   # Business logic
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/              # Flyway migrations (V1–V4)
│   └── src/test/java/                 # JUnit + Testcontainers
├── sdk/typescript/                    # Official TypeScript SDK
│   ├── src/
│   │   ├── client.ts                  # HTTP client
│   │   ├── errors.ts                  # Typed errors
│   │   ├── types.ts                   # Public types
│   │   └── resources/                 # One file per resource
│   └── examples/quickstart.ts
├── .github/workflows/ci.yml           # GitHub Actions
├── docker-compose.yml                 # Full stack
├── .env.example
└── README.md
```

---

## 🔐 Security

- API keys are hashed with SHA-256 before storage (never stored plain)
- API keys are compared using the hash (constant-time comparison)
- Multi-tenant isolation enforced at every query
- Rate limiting + quotas prevent abuse
- All input validated via Jakarta Bean Validation
- SQL injection prevented via parameterized queries (JPA/Hibernate)

**Reporting a vulnerability:** please open a private security advisory on GitHub.

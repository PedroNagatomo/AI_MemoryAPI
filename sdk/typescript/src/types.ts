/**
 * Tipos públicos do SDK.
 */

// ============ Requests ============

export interface RegisterTenantRequest {
  name: string;
  email: string;
}

export interface CreateEndUserRequest {
  externalId: string;
  metadata?: Record<string, unknown>;
}

export interface CreateMemoryRequest {
  endUserId: string;
  content: string;
  category?: string;
  source: string;
  sourceId?: string;
  importance?: number;
  metadata?: Record<string, unknown>;
}

export interface IngestRequest {
  endUserId: string;
  sourceId?: string;
  messages: ChatMessageInput[];
}

export interface ChatMessageInput {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface SearchRequest {
  endUserId: string;
  query: string;
  limit?: number;
  categories?: string[];
}

// ============ Responses ============

export interface RegisterTenantResponse {
  tenantId: string;
  name: string;
  email: string;
  apiKey: string;
  environment: string;
  plan: string;
}

export interface EndUserResponse {
  id: string;
  externalId: string;
  metadata: Record<string, unknown>;
  createdAt: string;
}

export interface MemoryResponse {
  id: string;
  endUserId: string;
  content: string;
  category: string | null;
  source: string;
  sourceId: string | null;
  importance: number;
  metadata: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export interface IngestResponse {
  extracted: number;
  persisted: number;
  tokensUsed: number | null;
  memories: MemoryResponse[];
}

export interface SearchResponse {
  query: string;
  count: number;
  memories: SearchHit[];
}

export interface SearchHit {
  id: string;
  endUserId: string;
  content: string;
  category: string | null;
  source: string;
  sourceId: string | null;
  importance: number;
  metadata: Record<string, unknown>;
  createdAt: string;
}

// ============ Config ============

export interface ClientOptions {
  apiKey: string;
  baseUrl?: string;
  timeout?: number;
  maxRetries?: number;
}

// ============ Usage ============

export interface UsageResponse {
  period: string | null;
  plan: string;

  memoriesUsed: number;
  memoriesQuota: number;
  memoriesPercent: number;

  tokensUsed: number;
  tokensQuota: number;
  tokensPercent: number;

  extractionsCount: number;
  searchesCount: number;
  apiCallsCount: number;

  resetAt: string;
}

export interface QuotaExceededDetails {
  quotaType: 'memories' | 'tokens';
  current: number;
  limit: number;
  plan: string;
  resetAt: string;
  upgradeUrl: string;
}
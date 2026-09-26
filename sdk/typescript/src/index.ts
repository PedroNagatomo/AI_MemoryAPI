// Entry point do SDK
export { AIMemoryClient } from "./client.js";

// Tipos
export type {
  ClientOptions,
  RegisterTenantRequest,
  RegisterTenantResponse,
  CreateEndUserRequest,
  EndUserResponse,
  CreateMemoryRequest,
  MemoryResponse,
  IngestRequest,
  IngestResponse,
  ChatMessageInput,
  SearchRequest,
  SearchResponse,
  SearchHit,
  UsageResponse,
  QuotaExceededDetails,
} from "./types.js";

// Erros
export {
  AIMemoryError,
  AuthenticationError,
  ForbiddenError,
  NotFoundError,
  ValidationError,
  RateLimitError,
  ServerError,
  NetworkError,
  QuotaExceededError,
} from "./errors.js";

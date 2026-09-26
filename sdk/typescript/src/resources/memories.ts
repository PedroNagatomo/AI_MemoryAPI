import type { AIMemoryClient } from '../client.js';
import type {
  CreateMemoryRequest,
  IngestRequest,
  IngestResponse,
  MemoryResponse,
  SearchRequest,
  SearchResponse,
} from '../types.js';

export class MemoriesResource {
  constructor(private readonly client: AIMemoryClient) {}

  /**
   * Cria uma memória manualmente.
   */
  create(data: CreateMemoryRequest): Promise<MemoryResponse> {
    return this.client.request<MemoryResponse>('POST', '/v1/memories', data);
  }

  /**
   * Lista memórias de um end-user (paginado).
   */
  list(
    endUserId: string,
    options?: { limit?: number; offset?: number }
  ): Promise<MemoryResponse[]> {
    const params = new URLSearchParams({
      endUserId,
      limit: String(options?.limit ?? 20),
      offset: String(options?.offset ?? 0),
    });
    return this.client.request<MemoryResponse[]>(
      'GET',
      `/v1/memories?${params}`
    );
  }

  /**
   * Busca memória por ID.
   */
  get(id: string): Promise<MemoryResponse> {
    return this.client.request<MemoryResponse>('GET', `/v1/memories/${id}`);
  }

  /**
   * Soft-delete de memória.
   */
  delete(id: string): Promise<void> {
    return this.client.request<void>('DELETE', `/v1/memories/${id}`);
  }

  /**
   * Ingest: envia uma conversa e a IA extrai memórias automaticamente.
   */
  ingest(data: IngestRequest): Promise<IngestResponse> {
    return this.client.request<IngestResponse>(
      'POST',
      '/v1/memories/ingest',
      data
    );
  }

  /**
   * Busca textual (full-text search) com ranking por relevância.
   */
  search(data: SearchRequest): Promise<SearchResponse> {
    return this.client.request<SearchResponse>(
      'POST',
      '/v1/memories/search',
      data
    );
  }
}